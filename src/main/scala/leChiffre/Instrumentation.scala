// See LICENSE.IBM for license details.

package leChiffre

import chisel3._
import chisel3.util._
import chisel3.internal.InstanceId
import chisel3.experimental.ChiselAnnotation
import scala.collection.mutable

trait ChiffreController {
  self: Module =>

  /** Scan Chain Identifier used to differentiate scan chains. This must
    * be a `lazy val`. */
  def scanId: String

  private def addSource(component: InstanceId, name: String): Unit = {
    annotate(
      ChiselAnnotation(component,
                       classOf[_root_.firrtl.passes.wiring.WiringTransform],
                       s"source $name"))
  }

  private def scanMaster(in: InstanceId, out: InstanceId, name: String): Unit = {
    if (scanId == null) {
      throw new Exception(
        "Chiffre Controller attribute 'scanId' was 'null' (should be a 'lazy val')") }
    annotate(
      ChiselAnnotation(
        in,
        classOf[leChiffre.passes.ScanChainTransform],
        s"master:in:$name"))
    annotate(
      ChiselAnnotation(
        out,
        classOf[leChiffre.passes.ScanChainTransform],
        s"master:out:$name"))
  }

  val scan = Wire(new ScanIo)

  addSource(scan.clk, "scan_clk")
  addSource(scan.en, "scan_en")
  scanMaster(scan.in, scan.out, scanId)
}

trait ChiffreInjectee {
  self: Module =>

  def isFaulty[T <: InjectorNBit](component: InstanceId, id: String,
                                  tpe: Class[T]): Unit = {
    component match {
      case c: Bits =>
        annotate(ChiselAnnotation(c,
          classOf[passes.FaultInstrumentationTransform],
          s"injector:$id:${tpe.getName}"))
      case c => throw new Exception(s"Type not implemented for: $c")
    }
  }
}
