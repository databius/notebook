package com.databius.notebook.scala

object DiReader extends App {
  ApplicationLiveReader.register("dtvd")(new UserRepositoryReader)
  ApplicationLiveReader.register("dtvd")(new MockUserRepositoryReader)
}

class UserRepositoryReader {
  def save(user: User) = {
    println("Saved " + user.name)
  }
}

class UserServiceReader {
  def register(name: String) =
    (r: UserRepositoryReader) => r.save(new User(name))
}

object ApplicationLiveReader extends UserServiceReader

class MockUserRepositoryReader extends UserRepositoryReader {
  override def save(user: User) = {
    println("Do nothing!!!")
  }
}
