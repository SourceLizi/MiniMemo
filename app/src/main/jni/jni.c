#include <jni.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/configuration.h>
#include <android/log.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/sysinfo.h>
#include <string.h>
#include "whisper.h"
#include "ggml.h"

#define UNUSED(x) (void)(x)
#define TAG "JNI"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,     TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,     TAG, __VA_ARGS__)

static inline int min(int a, int b) {
    return (a < b) ? a : b;
}

static inline int max(int a, int b) {
    return (a > b) ? a : b;
}

static bool is_aborted = false;
static char language[3] = {0};
static char country[3] = {0};

struct input_stream_context {
    size_t offset;
    JNIEnv * env;
    jobject thiz;
    jobject input_stream;

    jmethodID mid_available;
    jmethodID mid_read;
};

struct callback_context{
    JNIEnv * env;
    jobject thiz;
    jobject call_back;
    jmethodID call_method;
};

size_t inputStreamRead(void * ctx, void * output, size_t read_size) {
    struct input_stream_context* is = (struct input_stream_context*)ctx;

    jint avail_size = (*is->env)->CallIntMethod(is->env, is->input_stream, is->mid_available);
    jint size_to_copy = read_size < avail_size ? (jint)read_size : avail_size;

    jbyteArray byte_array = (*is->env)->NewByteArray(is->env, size_to_copy);

    jint n_read = (*is->env)->CallIntMethod(is->env, is->input_stream, is->mid_read, byte_array, 0, size_to_copy);

    if (size_to_copy != read_size || size_to_copy != n_read) {
        LOGI("Insufficient Read: Req=%zu, ToCopy=%d, Available=%d", read_size, size_to_copy, n_read);
    }

    jbyte* byte_array_elements = (*is->env)->GetByteArrayElements(is->env, byte_array, NULL);
    memcpy(output, byte_array_elements, size_to_copy);
    (*is->env)->ReleaseByteArrayElements(is->env, byte_array, byte_array_elements, JNI_ABORT);

    (*is->env)->DeleteLocalRef(is->env, byte_array);

    is->offset += size_to_copy;

    return size_to_copy;
}
bool inputStreamEof(void * ctx) {
    struct input_stream_context* is = (struct input_stream_context*)ctx;

    jint result = (*is->env)->CallIntMethod(is->env, is->input_stream, is->mid_available);
    return result <= 0;
}
void inputStreamClose(void * ctx) {

}

JNIEXPORT jlong JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_initContextFromInputStream(
        JNIEnv *env, jobject thiz, jobject input_stream) {
    UNUSED(thiz);

    struct whisper_context *context = NULL;
    struct whisper_model_loader loader = {};
    struct input_stream_context inp_ctx = {};

    inp_ctx.offset = 0;
    inp_ctx.env = env;
    inp_ctx.thiz = thiz;
    inp_ctx.input_stream = input_stream;

    jclass cls = (*env)->GetObjectClass(env, input_stream);
    inp_ctx.mid_available = (*env)->GetMethodID(env, cls, "available", "()I");
    inp_ctx.mid_read = (*env)->GetMethodID(env, cls, "read", "([BII)I");

    loader.context = &inp_ctx;
    loader.read = inputStreamRead;
    loader.eof = inputStreamEof;
    loader.close = inputStreamClose;

    loader.eof(loader.context);

    context = whisper_init(&loader);
    return (jlong) context;
}

static size_t asset_read(void *ctx, void *output, size_t read_size) {
    return AAsset_read((AAsset *) ctx, output, read_size);
}

static bool asset_is_eof(void *ctx) {
    return AAsset_getRemainingLength64((AAsset *) ctx) <= 0;
}

static void asset_close(void *ctx) {
    AAsset_close((AAsset *) ctx);
}

static struct whisper_context *whisper_init_from_asset(
        JNIEnv *env,
        jobject assetManager,
        const char *asset_path
) {
    LOGI("Loading model from asset '%s'\n", asset_path);
    AAssetManager *asset_manager = AAssetManager_fromJava(env, assetManager);
    AAsset *asset = AAssetManager_open(asset_manager, asset_path, AASSET_MODE_STREAMING);
    if (!asset) {
        LOGW("Failed to open '%s'\n", asset_path);
        return NULL;
    }

    whisper_model_loader loader = {
            .context = asset,
            .read = &asset_read,
            .eof = &asset_is_eof,
            .close = &asset_close
    };

    return whisper_init(&loader);
}

JNIEXPORT jlong JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_initContextFromAsset(
        JNIEnv *env, jobject thiz, jobject assetManager, jstring asset_path_str) {
    UNUSED(thiz);
    struct whisper_context *context = NULL;
    const char *asset_path_chars = (*env)->GetStringUTFChars(env, asset_path_str, NULL);

    AAssetManager *asset_manager = AAssetManager_fromJava(env, assetManager);
    AConfiguration* configuration = AConfiguration_new();
    AConfiguration_fromAssetManager(configuration,asset_manager);
    AConfiguration_getLanguage(configuration,language);
    AConfiguration_getCountry(configuration,country);

    context = whisper_init_from_asset(env, assetManager, asset_path_chars);
    (*env)->ReleaseStringUTFChars(env, asset_path_str, asset_path_chars);
    return (jlong) context;
}

JNIEXPORT jlong JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_initContext(
        JNIEnv *env, jobject thiz, jstring model_path_str) {
    UNUSED(thiz);
    struct whisper_context *context = NULL;
    const char *model_path_chars = (*env)->GetStringUTFChars(env, model_path_str, NULL);
    context = whisper_init_from_file(model_path_chars);
    (*env)->ReleaseStringUTFChars(env, model_path_str, model_path_chars);
    return (jlong) context;
}

void on_new_segment(struct whisper_context * ctx, struct whisper_state * state
        , int n_new, void * user_data){
    if(is_aborted) return;
    struct callback_context* cb_context = (struct callback_context*)user_data;
    int n_segments = whisper_full_n_segments(ctx);
    int s0 = n_segments - n_new;
    for (int i = s0; i < n_segments; i++) {
        const char * text = whisper_full_get_segment_text(ctx, i);
        jstring text_str = (*cb_context->env)->NewStringUTF(cb_context->env, text);
        (*cb_context->env)->CallVoidMethod(cb_context->env, cb_context->call_back
                        , cb_context->call_method,text_str,(jint)i);
        (*cb_context->env)->DeleteLocalRef(cb_context->env,text_str);
    }
}

JNIEXPORT void JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_freeContext(
        JNIEnv *env, jobject thiz, jlong context_ptr) {
    UNUSED(env);
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    whisper_free(context);
}

bool whisper_abort_callback(void * user_data) {
    bool is_aborted = *(bool*)user_data;
    return is_aborted;
};

bool whisper_encbegin_callback(struct whisper_context * ctx, struct whisper_state * state, void * user_data) {
    bool is_aborted = *(bool*)user_data;
    return !is_aborted;
};

JNIEXPORT void JNICALL
        Java_com_whispercpp_java_whisper_WhisperLib_setAbort(JNIEnv *env, jobject thiz){
    UNUSED(thiz);
    is_aborted = true;
}

JNIEXPORT void JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_fullTranscribe(
        JNIEnv *env, jobject thiz, jlong context_ptr, jint num_threads,
        jfloatArray audio_data, jobject new_seg_cb) {
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    struct callback_context cb_context = {};
    jclass callClass = NULL;
    jfloat *audio_data_arr = (*env)->GetFloatArrayElements(env, audio_data, NULL);
    const jsize audio_data_length = (*env)->GetArrayLength(env, audio_data);

    struct whisper_full_params params = whisper_full_default_params(WHISPER_SAMPLING_BEAM_SEARCH);
    params.print_realtime = false;
    params.print_progress = false;
    params.print_timestamps = false;
    params.print_special = false;
    params.translate = false;
    params.detect_language = false;
    if(language[0] != '\0' && language[1] != '\0'){
        params.language = language;
        if(language[0] == 'z' && language[1] == 'h'
            && country[0] == 'C' && country[1] == 'N'){
            params.initial_prompt = "以下是普通话的句子。";
        }
    }else{
        params.language = "en";
    }

    //params.n_threads = num_threads;
    params.no_timestamps = true;
    params.offset_ms = 0;
    params.duration_ms = audio_data_length / 16;
    params.no_context = true;
    params.single_segment = false;
    params.suppress_blank = true;
    params.suppress_non_speech_tokens = true;

    params.abort_callback = whisper_abort_callback;
    params.abort_callback_user_data = &is_aborted;

    params.encoder_begin_callback = whisper_encbegin_callback;
    params.encoder_begin_callback_user_data = &is_aborted;
    is_aborted = false;

    if(new_seg_cb){
        cb_context.env = env;
        cb_context.thiz = thiz;
        cb_context.call_back = new_seg_cb;

        callClass = (*env)->GetObjectClass(env,new_seg_cb);
        cb_context.call_method = (*env)->GetMethodID(env,callClass
                ,"onNewSegment","(Ljava/lang/String;I)V");

        params.new_segment_callback = on_new_segment;
        params.new_segment_callback_user_data = &cb_context;
    }
    whisper_reset_timings(context);

    LOGI("About to run whisper_full, locale=%s-%s",language,country);

    if (whisper_full(context, params, audio_data_arr, audio_data_length) != 0) {
        LOGI("Failed to run the model");
    }
//    else {
//        whisper_print_timings(context);
//    }

    if(callClass != NULL){
        (*env)->DeleteLocalRef(env,callClass);
    }
    (*env)->ReleaseFloatArrayElements(env, audio_data, audio_data_arr, JNI_ABORT);
}

JNIEXPORT jint JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_getTextSegmentCount(
        JNIEnv *env, jobject thiz, jlong context_ptr) {
    UNUSED(env);
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    return whisper_full_n_segments(context);
}


JNIEXPORT jstring JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_getTextSegment(
        JNIEnv *env, jobject thiz, jlong context_ptr, jint index) {
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    const char *text = whisper_full_get_segment_text(context, index);
    jstring string = (*env)->NewStringUTF(env, text);
    return string;
}

JNIEXPORT jlong JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_getTextSegmentT0(JNIEnv *env, jobject thiz,jlong context_ptr, jint index) {
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    const int64_t t0 = whisper_full_get_segment_t0(context, index);
    return (jlong)t0;
}

JNIEXPORT jlong JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_getTextSegmentT1(JNIEnv *env, jobject thiz,jlong context_ptr, jint index) {
    UNUSED(thiz);
    struct whisper_context *context = (struct whisper_context *) context_ptr;
    const int64_t t1 = whisper_full_get_segment_t1(context, index);
    return (jlong)t1;
}

JNIEXPORT jstring JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_getSystemInfo(
        JNIEnv *env, jobject thiz
) {
    UNUSED(thiz);
    const char *sysinfo = whisper_print_system_info();
    jstring string = (*env)->NewStringUTF(env, sysinfo);
    return string;
}

JNIEXPORT jstring JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_benchMemcpy(JNIEnv *env, jobject thiz,
                                                                      jint n_threads) {
    UNUSED(thiz);
    const char *bench_ggml_memcpy = whisper_bench_memcpy_str(n_threads);
    jstring string = (*env)->NewStringUTF(env, bench_ggml_memcpy);

    return string;
}

JNIEXPORT jstring JNICALL
Java_com_whispercpp_java_whisper_WhisperLib_benchGgmlMulMat(JNIEnv *env, jobject thiz,
                                                                          jint n_threads) {
    UNUSED(thiz);
    const char *bench_ggml_mul_mat = whisper_bench_ggml_mul_mat_str(n_threads);
    jstring string = (*env)->NewStringUTF(env, bench_ggml_mul_mat);

    return string;
}

