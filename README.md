# calev
[![Build Status](https://travis-ci.org/eikek/calev.svg?branch=master)](https://travis-ci.org/eikek/calev)
[![Scaladex](https://index.scala-lang.org/eikek/calev/latest.svg?color=blue)](https://index.scala-lang.org/eikek/calev/calev-core)
[![Scala Steward badge](https://img.shields.io/badge/Scala_Steward-helping-blue.svg?style=flat&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAQCAMAAAARSr4IAAAAVFBMVEUAAACHjojlOy5NWlrKzcYRKjGFjIbp293YycuLa3pYY2LSqql4f3pCUFTgSjNodYRmcXUsPD/NTTbjRS+2jomhgnzNc223cGvZS0HaSD0XLjbaSjElhIr+AAAAAXRSTlMAQObYZgAAAHlJREFUCNdNyosOwyAIhWHAQS1Vt7a77/3fcxxdmv0xwmckutAR1nkm4ggbyEcg/wWmlGLDAA3oL50xi6fk5ffZ3E2E3QfZDCcCN2YtbEWZt+Drc6u6rlqv7Uk0LdKqqr5rk2UCRXOk0vmQKGfc94nOJyQjouF9H/wCc9gECEYfONoAAAAASUVORK5CYII=)](https://scala-steward.org)

Small Scala library for parsing systemd.time like calendar event
expressions. It is available for Scala (JVM and ScalaJS) 2.12, 2.13
and 3.0. The core module has no dependencies.

## What are calendar events?

It serves the same purpose as cron expressions, but uses a different
syntax: a "normal" timestamp where each part is a pattern. A pattern
is a list of values, a range or `*` meaning every value. Some
examples:

| Expression                  | Meaning                                                      |
|-----------------------------|--------------------------------------------------------------|
| `*-*-* 12:15:00`            | every day at 12:15                                           |
| `2020-1,5,9-* 10:00:00`     | every day on Jan, May and Sept of 2020 at 10:00              |
| `Mon *-*-* 09:00:00`        | every monday at 9:00                                         |
| `Mon..Fri *-*-1/7 15:00:00` | on 1.,8.,15. etc of every month at 15:00 but not on weekends |

The `1/7` means value `1` and all multiples of `7` added to it. A
range with repetition, like `1..12/2` means `1` and all multiples of
`2` addet to it within the range `1..12`.

For more information see

```shell
man systemd.time
```

or

<https://man.cx/systemd.time#heading7>


## Limitations

This library has some limitations when parsing calendar events
compared to systemd:

- The `~` in the date part for refering last days of a month is not
  supported.
- No parts except weekdays may be absent. Date and time parts must all
  be specified, except seconds are optional.

## Modules

- The *core* module has zero dependencies and implements the parser
  and generator for calendar events. It is also published for ScalaJS.
  With sbt, use:
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-core" % "0.5.4"
  ```
- The *fs2* module contains utilities to work with
  [FS2](https://github.com/functional-streams-for-scala/fs2) streams.
  These were taken, thankfully and slightly modified to exchange cron expressions
  for calendar events, from the
  [fs2-cron](https://github.com/fthomas/fs2-cron) library.  It is also published
  for ScalaJS. With sbt, use
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-fs2" % "0.5.4"
  ```
- The *doobie* module contains `Meta`, `Read` and `Write` instances
  for `CalEvent` to use with
  [doobie](https://github.com/tpolecat/doobie).
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-doobie" % "0.5.4"
  ```
- The *circe* module defines a json decoder and encoder for `CalEvent`
  instances to use with [circe](https://github.com/circe/circe).  It is also
  published for ScalaJS.
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-circe" % "0.5.4"
  ```
- The *jackson* module defines `CalevModule` for [Jackson](https://github.com/FasterXML/jackson)
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-jackson" % "0.5.4"
  ```
- The *akka* module allows to use calendar events with [Akka Scheduler](https://doc.akka.io/docs/akka/current/scheduler.html)
  and [Akka Timers](https://doc.akka.io/docs/akka/current/typed/interaction-patterns.html#typed-scheduling). 
  ```sbt
  libraryDependencies += "com.github.eikek" %% "calev-akka" % "0.5.4"
  ```


## Examples

### Core

Calendar events can be read from a string:

```scala
import com.github.eikek.calev._

CalEvent.parse("Mon..Fri *-*-* 6,14:0:0")
// res0: Either[String, CalEvent] = Right(
//   value = CalEvent(
//     weekday = List(
//       values = Vector(Range(range = WeekdayRange(start = Mon, end = Fri)))
//     ),
//     date = DateEvent(year = All, month = All, day = All),
//     time = TimeEvent(
//       hour = List(
//         values = Vector(
//           Single(value = 6, rep = None),
//           Single(value = 14, rep = None)
//         )
//       ),
//       minute = List(values = Vector(Single(value = 0, rep = None))),
//       seconds = List(values = Vector(Single(value = 0, rep = None)))
//     ),
//     zone = None
//   )
// )

CalEvent.parse("Mon *-*-* 6,88:0:0")
// res1: Either[String, CalEvent] = Left(
//   value = "Value 88 not in range [0,23]"
// )
```

There is an `unsafe` way that throws exceptions:

```scala
CalEvent.unsafe("*-*-* 0/2:0:0")
// res2: CalEvent = CalEvent(
//   weekday = All,
//   date = DateEvent(year = All, month = All, day = All),
//   time = TimeEvent(
//     hour = List(values = Vector(Single(value = 0, rep = Some(value = 2)))),
//     minute = List(values = Vector(Single(value = 0, rep = None))),
//     seconds = List(values = Vector(Single(value = 0, rep = None)))
//   ),
//   zone = None
// )
```

There is a tiny dsl for more conveniently defining events in code:

```scala
import com.github.eikek.calev.Dsl._

val ce = CalEvent(AllWeekdays, DateEvent.All, time(0 #/ 2, 0.c, 0.c))
// ce: CalEvent = CalEvent(
//   weekday = All,
//   date = DateEvent(year = All, month = All, day = All),
//   time = TimeEvent(
//     hour = List(values = List(Single(value = 0, rep = Some(value = 2)))),
//     minute = List(values = List(Single(value = 0, rep = None))),
//     seconds = List(values = List(Single(value = 0, rep = None)))
//   ),
//   zone = None
// )
ce.asString
// res3: String = "*-*-* 00/2:00:00"
```

Once there is a calendar event, the times it will elapse next can be
generated:

```scala
import java.time._

ce.asString
// res4: String = "*-*-* 00/2:00:00"
val now = LocalDateTime.now
// now: LocalDateTime = 2021-06-30T00:21:12.170
ce.nextElapse(now)
// res5: Option[LocalDateTime] = Some(value = 2021-06-30T02:00)
ce.nextElapses(now, 5)
// res6: List[LocalDateTime] = List(
//   2021-06-30T02:00,
//   2021-06-30T04:00,
//   2021-06-30T06:00,
//   2021-06-30T08:00,
//   2021-06-30T10:00
// )
```

If an event is in the past, the `nextElapsed` returns a `None`:

```scala
CalEvent.unsafe("1900-01-* 12,14:0:0").nextElapse(LocalDateTime.now)
// res7: Option[LocalDateTime] = None
```


### FS2

The fs2 utilities allow to schedule things based on calendar events.
This is the same as [fs2-cron](https://github.com/fthomas/fs2-cron)
provides, only adopted to use calendar events instead of cron
expressions. The example is also from there.

**Note:** `calev-fs2` is still build against fs2 2.x. This module will
be removed in the future, because the
[fs2-cron](https://github.com/fthomas/fs2-cron) project now provides
this via its `fs2-cron-calev` module, which is built against fs2 3
already.

```scala
import cats.effect.{IO, Timer}
import fs2.Stream
import com.github.eikek.fs2calev._
import java.time.LocalTime
import scala.concurrent.ExecutionContext

implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
// timer: Timer[IO] = cats.effect.internals.IOTimer@4406d447

val printTime = IO(println(LocalTime.now))
// printTime: IO[Unit] = Delay(thunk = <function0>)

val event = CalEvent.unsafe("*-*-* *:*:0/2")
// event: CalEvent = CalEvent(
//   weekday = All,
//   date = DateEvent(year = All, month = All, day = All),
//   time = TimeEvent(
//     hour = All,
//     minute = All,
//     seconds = List(values = Vector(Single(value = 0, rep = Some(value = 2))))
//   ),
//   zone = None
// )

val task = CalevFs2.awakeEvery[IO](event).evalMap(_ => printTime)
// task: Stream[IO[x], Unit] = Stream(..)

task.take(3).compile.drain.unsafeRunSync()
// 00:21:14.021
// 00:21:16
// 00:21:18.001
```


### Doobie

When using doobie, this module contains instances to write and read
calendar event expressions through SQL.

```scala
import com.github.eikek.calev._
import com.github.eikek.calev.doobie.CalevDoobieMeta._
import _root_.doobie._
import _root_.doobie.implicits._

case class Record(event: CalEvent)

val r = Record(CalEvent.unsafe("Mon *-*-* 0/2:15"))
// r: Record = Record(
//   event = CalEvent(
//     weekday = List(values = Vector(Single(day = Mon))),
//     date = DateEvent(year = All, month = All, day = All),
//     time = TimeEvent(
//       hour = List(values = Vector(Single(value = 0, rep = Some(value = 2)))),
//       minute = List(values = Vector(Single(value = 15, rep = None))),
//       seconds = List(values = List(Single(value = 0, rep = None)))
//     ),
//     zone = None
//   )
// )

val insert =
  sql"INSERT INTO mytable (event) VALUES (${r.event})".update.run
// insert: ConnectionIO[Int] = Suspend(
//   a = BracketCase(
//     acquire = Suspend(
//       a = PrepareStatement(a = "INSERT INTO mytable (event) VALUES (?)")
//     ),
//     use = doobie.hi.connection$$$Lambda$11625/2046523540@5c2fb3a6,
//     release = cats.effect.Bracket$$Lambda$11627/540350284@8c0f281
//   )
// )

val select =
  sql"SELECT event FROM mytable WHERE id = 1".query[Record].unique
// select: ConnectionIO[Record] = Suspend(
//   a = BracketCase(
//     acquire = Suspend(
//       a = PrepareStatement(a = "SELECT event FROM mytable WHERE id = 1")
//     ),
//     use = doobie.hi.connection$$$Lambda$11625/2046523540@1c03aa81,
//     release = cats.effect.Bracket$$Lambda$11627/540350284@430a5324
//   )
// )
```


### Circe

The defined encoders/decoders can be put in scope to use calendar
event expressions in json.

```scala
import com.github.eikek.calev._
import com.github.eikek.calev.circe.CalevCirceCodec._
import io.circe._
import io.circe.generic.semiauto._
import io.circe.syntax._

case class Meeting(name: String, event: CalEvent)
object Meeting {
  implicit val jsonDecoder = deriveDecoder[Meeting]
  implicit val jsonEncoder = deriveEncoder[Meeting]
}

val meeting = Meeting("trash can", CalEvent.unsafe("Mon..Fri *-*-* 14,18:0"))
// meeting: Meeting = Meeting(
//   name = "trash can",
//   event = CalEvent(
//     weekday = List(
//       values = Vector(Range(range = WeekdayRange(start = Mon, end = Fri)))
//     ),
//     date = DateEvent(year = All, month = All, day = All),
//     time = TimeEvent(
//       hour = List(
//         values = Vector(
//           Single(value = 14, rep = None),
//           Single(value = 18, rep = None)
//         )
//       ),
//       minute = List(values = Vector(Single(value = 0, rep = None))),
//       seconds = List(values = List(Single(value = 0, rep = None)))
//     ),
//     zone = None
//   )
// )
val json = meeting.asJson.noSpaces
// json: String = "{\"name\":\"trash can\",\"event\":\"Mon..Fri *-*-* 14,18:00:00\"}"
val read = for {
  parsed <- parser.parse(json)
  value <- parsed.as[Meeting]
} yield value
// read: Either[Error, Meeting] = Right(
//   value = Meeting(
//     name = "trash can",
//     event = CalEvent(
//       weekday = List(
//         values = Vector(Range(range = WeekdayRange(start = Mon, end = Fri)))
//       ),
//       date = DateEvent(year = All, month = All, day = All),
//       time = TimeEvent(
//         hour = List(
//           values = Vector(
//             Single(value = 14, rep = None),
//             Single(value = 18, rep = None)
//           )
//         ),
//         minute = List(values = Vector(Single(value = 0, rep = None))),
//         seconds = List(values = Vector(Single(value = 0, rep = None)))
//       ),
//       zone = None
//     )
//   )
// )
```
### Jackson

Add `CalevModule` to use calendar event expressions in json: 

```scala
import com.github.eikek.calev._
import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.json.JsonMapper
import com.github.eikek.calev.jackson.CalevModule

val jackson = JsonMapper
  .builder()
  .addModule(new CalevModule())
  .build()
// jackson: JsonMapper = com.fasterxml.jackson.databind.json.JsonMapper@62197a60

val myEvent    = CalEvent.unsafe("Mon *-*-* 05:00/10:00")
// myEvent: CalEvent = CalEvent(
//   weekday = List(values = Vector(Single(day = Mon))),
//   date = DateEvent(year = All, month = All, day = All),
//   time = TimeEvent(
//     hour = List(values = Vector(Single(value = 5, rep = None))),
//     minute = List(values = Vector(Single(value = 0, rep = Some(value = 10)))),
//     seconds = List(values = Vector(Single(value = 0, rep = None)))
//   ),
//   zone = None
// )

val eventSerialized = jackson.writeValueAsString(myEvent)
// eventSerialized: String = "\"Mon *-*-* 05:00/10:00\""
val eventDeserialized = jackson.readValue(eventSerialized, new TypeReference[CalEvent] {})
// eventDeserialized: CalEvent = CalEvent(
//   weekday = List(values = Vector(Single(day = Mon))),
//   date = DateEvent(year = All, month = All, day = All),
//   time = TimeEvent(
//     hour = List(values = Vector(Single(value = 5, rep = None))),
//     minute = List(values = Vector(Single(value = 0, rep = Some(value = 10)))),
//     seconds = List(values = Vector(Single(value = 0, rep = None)))
//   ),
//   zone = None
// )
```
### Akka

#### Akka Timers

When building actor behavior, use ```CalevBehaviors.withCalevTimers``` 
to get access to ```CalevTimerScheduler```.

Use ```CalevTimerScheduler``` to start single Akka Timer 
for the upcoming event according to given calendar event definition.

```scala
import com.github.eikek.calev.CalEvent
import java.time._
import com.github.eikek.calev.akka._
import com.github.eikek.calev.akka.dsl.CalevBehaviors
import _root_.akka.actor.typed._
import _root_.akka.actor.typed.scaladsl.Behaviors._

sealed trait Message
case class Tick(timestamp: ZonedDateTime) extends Message
case class Ping()                         extends Message

// every day, every full minute
def calEvent   = CalEvent.unsafe("*-*-* *:0/1:0")  

CalevBehaviors.withCalevTimers[Message]() { scheduler =>
  scheduler.startSingleTimer(calEvent, Tick)
    receiveMessage[Message] {
      case tick: Tick =>
        println(
          s"Tick scheduled at ${tick.timestamp.toLocalTime} received at: ${LocalTime.now}"
        )
        same
      case ping: Ping =>
        println("Ping received")
        same
    }
}
// res9: Behavior[Message] = Deferred(TimerSchedulerImpl.scala:29)
```

Use ```CalevBehaviors.withCalendarEvent``` to schedule messages according 
to the given calendar event definition.   

```scala
CalevBehaviors.withCalendarEvent(calEvent)(
  Tick,
  receiveMessage[Message] {
    case tick: Tick =>
      println(
        s"Tick scheduled at ${tick.timestamp.toLocalTime} received at: ${LocalTime.now}"
      )
      same
    case ping: Ping =>
      println("Ping received")
      same
  }
)
// res10: Behavior[Message] = Deferred(InterceptorImpl.scala:29-30)
```
#### Testing

See [CalevBehaviorsTest](https://github.com/eikek/calev/blob/master/modules/akka/src/test/scala/com/github/eikek/calev/akka/dsl/CalevBehaviorsTest.scala)

#### Akka Scheduler 

Schedule the sending of a message to the given target Actor at the time of 
the upcoming event according to the given calendar event definition.

```scala
def behavior(tickReceiver: ActorRef[Tick]): Behavior[Message] = 
  setup { actorCtx =>
    actorCtx.scheduleOnceWithCalendarEvent(calEvent, tickReceiver, Tick)
    same
  }
```

Schedule the running of a ```Runnable``` at the time of the upcoming 
event according to the given calendar event definition.

```scala
implicit val system: ActorSystem[_] = ActorSystem(empty, "my-system")
// system: ActorSystem[_] = akka://my-system
import system.executionContext

calevScheduler().scheduleOnceWithCalendarEvent(calEvent, () => {
  println(
      s"Called at: ${LocalTime.now}"
  )
})
// res11: Option[<none>.<root>.akka.actor.Cancellable] = Some(
//   value = akka.actor.LightArrayRevolverScheduler$TaskHolder@48d6430
// )
system.terminate()
```