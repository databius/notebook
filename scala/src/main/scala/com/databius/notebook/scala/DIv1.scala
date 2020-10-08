package com.databius.notebook.scala

object DIv1 extends App {
  ApplicationLive.register("dtvd") // Saved dtvd
}

class User(val name: String)

class UserRepository {
  // Maybe DB initialization here
  def save(user: User) = {
    // Can use real DB access here
    println("Saved " + user.name)
  }
}

class UserService {
  val userRepository = new UserRepository
  def register(name: String) = {
    val someone = new User(name)
    userRepository.save(someone)
  }
}

object ApplicationLive extends UserService
