//
// Created by user on 05.08.19.
//

#ifndef JNI_JSINSTANCE_H
#define JNI_JSINSTANCE_H



#include <map>
#include <mutex>

class JSInstance {
public:
    JSInstance(JNIEnv *env, jobject thiz, JSContextRef ctx,
             JSClassDefinition def = kJSClassDefinitionEmpty, JSStringRef name = NULL);
    virtual ~JSInstance();
    virtual long getObjRef() { return (long) objRef; }
    static JSInstance* getInstance(JSObjectRef objref);

protected:
    JavaVM *jvm;
    jobject thiz;

private:
    JSObjectRef objRef;
    JSClassRef classRef;
    JSClassDefinition definition;

    static std::map<JSObjectRef,JSInstance *> objMap;
    static std::mutex mutex;

    static void StaticFinalizeCallback(JSObjectRef object);
};




#endif