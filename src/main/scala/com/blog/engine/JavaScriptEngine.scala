package com.blog.engine

abstract class JavaScriptEngine(scripts: Seq[ScriptSource]) {
  def invokeMethod[T](objectName: String, methodName: String, args: Any*): T
}