package com.paulbutcher

import akka.actor._
import akka.cluster._
import akka.cluster.ClusterEvent._

class TestActor extends Actor {

  def receive = {
    case x => println(s"Event received: $x")
  }
}

object ClusterTest extends App {

  val usage = """Options:
    -h hostname    Local hostname
    -p port        Local port
    -H hostname    Cluster hostname
    -P port        Closter port"""

  def options(list: List[String], map: Map[Symbol, String]): Map[Symbol, String] = list match {
    case "--help" :: _ => println(usage); sys.exit(0)
    case "-h" :: hostname :: rest => options(rest, map + ('localHost -> hostname))
    case "-p" :: port :: rest => options(rest, map + ('localPort -> port))
    case "-H" :: hostname :: rest => options(rest, map + ('clusterHost -> hostname))
    case "-P" :: port :: rest => options(rest, map + ('clusterPort -> port))
    case option :: _ => println(s"Unrecognised option: $option"); sys.exit(0)
    case Nil => map
  }
  val opts = options(args.toList, Map())

  System.setProperty("akka.remote.netty.hostname", opts('localHost))
  System.setProperty("akka.remote.netty.port", opts('localPort))

  val system = ActorSystem("ClusterTest")
  if (opts.contains('clusterHost))
    Cluster(system).join(Address("akka", "ClusterTest", opts('clusterHost), opts('clusterPort).toInt))

  val testActor = system.actorOf(Props[TestActor])
  Cluster(system).subscribe(testActor, classOf[MemberEvent])
}