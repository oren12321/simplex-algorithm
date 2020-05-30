cmake_minimum_required(VERSION 3.17)

find_package(Java REQUIRED)
include(UseJava)

project(simplex NONE)

set(CMAKE_JAVA_COMPILER_FLAGS -source 14 -target 14)

add_jar(simplex
    SOURCES
    Tester.java
    SimplexSimulation.java
    Simplex.java
    SimLog.java
    Rational.java
    ObjectiveFunction.java
    Constraint.java
    ENTRY_POINT Tester)