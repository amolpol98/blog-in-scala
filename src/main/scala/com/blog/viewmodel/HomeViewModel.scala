package com.blog.viewmodel

final case class HomeViewModel(greeting: String, title: String) extends ViewModel(title) {
  def this(greeting: String) = this(greeting, "Home")
}
