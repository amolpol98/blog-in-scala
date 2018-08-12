package com.blog.web.engine

import com.blog.engine.ScriptSource

abstract class JavaScriptEngine {
  def registerScripts(scripts: Seq[ScriptSource]): JavaScriptEngine
  def invokeMethod[T](objectName: String, methodName: String, args: Any*): T
  def build: JavaScriptEngine
  def destroy: Unit = {

  }
}