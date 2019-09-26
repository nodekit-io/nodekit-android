#include <jni.h>
#include <JavaScriptCore/JSObjectRef.h>
#include "JSInstance.h"

JSInstance::JSInstance(JNIEnv *env, jobject thiz, JSContextRef ctx,
                   JSClassDefinition def, JSStringRef name)
{
    env->GetJavaVM(&jvm);
    definition = def;
    definition.finalize = StaticFinalizeCallback;
    classRef = JSClassCreate(&definition);
    objRef = JSObjectMake(ctx, classRef, name);
    JSValueProtect(ctx, objRef);
    this->thiz = env->NewWeakGlobalRef(thiz);

    mutex.lock();
    objMap[objRef] = this;
    mutex.unlock();
}

JSInstance::~JSInstance()
{
    JSClassRelease(classRef);
    JNIEnv *env;
    int getEnvStat = jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
    if (getEnvStat == JNI_EDETACHED) {
        jvm->AttachCurrentThread(&env, NULL);
    }
    env->DeleteWeakGlobalRef(thiz);

    mutex.lock();
    objMap.erase(objRef);
    mutex.unlock();

    if (getEnvStat == JNI_EDETACHED) {
        jvm->DetachCurrentThread();
    }
}

JSInstance* JSInstance::getInstance(JSObjectRef objref)
{
    JSInstance *inst = NULL;
    mutex.lock();
    inst = objMap[objref];
    mutex.unlock();
    return inst;
}

std::map<JSObjectRef,JSInstance *> JSInstance::objMap = std::map<JSObjectRef,JSInstance *>();
std::mutex JSInstance::mutex;

void JSInstance::StaticFinalizeCallback(JSObjectRef object)
{
    JSInstance *thiz = getInstance(object);

    if (thiz) {
        delete thiz;
    }
}
