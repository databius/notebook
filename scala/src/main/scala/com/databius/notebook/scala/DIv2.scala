package com.databius.notebook.scala

object DIv2 extends App {
  ApplicationLiveV2.userService.register("dtvd")
}

trait UserRepositoryComponent {
  val userRepository = new UserRepository // <-- Here
  class UserRepository {
    def save(user: User) = {
      println("Saved " + user.name)
    }
  }
}

trait UserServiceComponent extends UserRepositoryComponent {
  val userService = new UserService // <-- Here
  class UserService {
    def register(name: String) = {
      val someone = new User(name)
      userRepository.save(someone)
    }
  }
}

object ApplicationLiveV2 extends UserServiceComponent
