package com.github.eikek.calev.akka

import java.time.{Clock, ZonedDateTime}

import scala.concurrent.duration._
import scala.reflect.ClassTag

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{Behaviors, TimerScheduler}
import com.github.eikek.calev.CalEvent

object CalevTimerScheduler {

  def withCalevTimers[T](
      minInterval: Option[FiniteDuration] = None,
      clock: Clock = Clock.systemDefaultZone()
  )(factory: CalevTimerScheduler[T] => Behavior[T]): Behavior[T] =
    Behaviors.withTimers { scheduler =>
      factory(new CalevTimerSchedulerImpl[T](scheduler, clock, minInterval))
    }

  def withCalendarEvent[I, O <: I: ClassTag](
      calEvent: CalEvent,
      clock: Clock = Clock.systemDefaultZone()
  )(triggerFactory: ZonedDateTime => O, inner: Behavior[I]): Behavior[I] =
    Behaviors
      .intercept(() => new CalevInterceptor[I, O](clock, calEvent, triggerFactory))(inner).asInstanceOf[Behavior[I]]

}

trait CalevTimerScheduler[T] {
  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit
}

private[akka] class CalevTimerSchedulerImpl[T](
    scheduler: TimerScheduler[T],
    clock: Clock,
    minInterval: Option[FiniteDuration]
) extends CalevTimerScheduler[T] {

  private val upcomingEventProvider =
    new UpcomingEventProvider(clock, minInterval)

  def scheduleUpcoming(calEvent: CalEvent, triggerFactory: ZonedDateTime => T): Unit =
    upcomingEventProvider(calEvent)
      .foreach { case (instant, delay) =>
        scheduler.startSingleTimer(triggerFactory.apply(instant), delay)
      }

}
