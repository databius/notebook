package com.databius.notebook.scala

object DIv3 extends App {
  ApplicationLiveV3.userService.register("dtvd")

  Test.userService.register("dtvd")
}

trait UserRepositoryComponentV3 {
  val userRepository: UserRepository
  class UserRepository {
    def save(user: User) = {
      println("Saved " + user.name)
    }
  }
}

trait UserServiceComponentV3 {
  this: UserRepositoryComponentV3 =>
  val userService: UserService
  class UserService {
    def register(name: String) = {
      val someone = new User(name)
      userRepository.save(someone)
    }
  }
}

object ApplicationLiveV3
    extends UserServiceComponentV3
    with UserRepositoryComponentV3 {
  val userRepository = new UserRepository
  val userService    = new UserService
}

object Test extends UserServiceComponentV3 with UserRepositoryComponentV3 {

  class MockUserRepository extends UserRepository {
    override def save(user: User) = {
      print("Do nothing!!!")
    }
  }
  val userRepository = new MockUserRepository
  val userService    = new UserService
}
