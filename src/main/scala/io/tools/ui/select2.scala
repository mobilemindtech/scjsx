package io.tools.ui

import com.raquo.airstream.core.{Observer, Signal}
import com.raquo.airstream.ownership.DynamicSubscription
import com.raquo.airstream.state.Var
import com.raquo.laminar.api.L.*
import io.tools.facede.Select2.*
import io.tools.facede.Select2JQuery.*
import org.querki.jquery.$
import org.scalajs.dom
import org.scalajs.dom.{HTMLButtonElement, HTMLOptionElement, window}

import scala.scalajs.js

object select2:


  /**
   * {{{
   *   formSelect2(
   *     "elementId",
   *     ajaxFn = () => "/api/search/json",
   *     stateVar.signal.map(
   *       _.field.map(c => List(Select2Data(id = c.id, text = c.text)))
   *     ),
   *     writer[Option[List[Select2Data]]]((state, data) =>
   *       data
   *         .flatMap(_.headOption)
   *         .map(c => state.copy(field = Some(My(id = c.id, text = c.text))))
   *         .getOrElse(state.copy(field = None))
   *     )
   *   ).node
   * }}}
   *
   * @param id Element ID
   * @param ajaxFn Callback should return api URL
   * @param reader Set select2 data
   * @param writer Item selected
   * @param multiple Select multiple
   */
  class formSelect2(id: String,
                     ajaxFn: () => String,
                     reader: Signal[Option[List[Select2Data]]],
                     writer: Observer[Option[List[Select2Data]]],
                     multiple: Boolean = false
                   ):

    // usado para conectar eventos de subscriptions
    private val dynOwner = new DynamicOwner(() => ())

    // connecta o evento de validação do code no eventBuss
    private val dynSub = DynamicSubscription.unsafe(
      dynOwner,
      activate = (owner: Owner) =>
        reader.addObserver(Observer { data =>
          val items = dataToValue(data)
          val newValue = items.map(_.id).mkString(",")
          if shouldChange(newValue) then makeChange(items, newValue)
        })(using owner)
    )

    private def el = $(s"#$id")

    private def newOption: HTMLOptionElement =
      dom.document
        .createElement("option")
        .asInstanceOf[HTMLOptionElement]

    private def newOption(d: Select2Data): HTMLOptionElement =
      val opt = newOption
      opt.value = d.id
      opt.textContent = d.text
      opt

    private def makeChange(data: List[Select2Data], newValue: String) =
      el
        .empty()
        .append(data.map(newOption)*)
        .value(js.Array(data.map(_.id)*))
        .trigger("change")

    private def dataToValue(data: Option[List[Select2Data]]) = data.getOrElse(List())

    private def shouldChange(newValue: String) =
      Option(el.value()).map(_.toString).getOrElse("") != newValue

    lazy val node: HtmlElement =
      select(
        idAttr(id),
        nameAttr(id),
        cls("form-control"),
        onMountCallback(ctx => onMount(ctx)),
        onUnmountCallback(el => unMount(el))
      )

    private def unMount(el: HtmlElement): Unit =
      dynOwner.deactivate()
      $(el.ref).select2("destroy")

    private def newOptions: Select2Opts =
      Select2Opts(
        placeholder = "Selecione...",
        allowClear = true,
        minimumInputLength = 2,
        multiple = multiple,
        ajax = Select2Ajax(ajaxFn)
      )

    private def onMount(ctx: MountContext[HtmlElement]): Unit =
      $(ctx.thisNode.ref)
        .select2(newOptions)
        .select2Change(writer.onNext)
      dynOwner.activate()
