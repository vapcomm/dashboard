/**
 * (c) VAP Communications Group, 2020
 */

package online.vapcom.dashboard.data

/**
 * Описание ошибки, которое передаётся для отображения и обработки с нижних уровней в UI
 */
data class ErrorDescription (
    val fatal: Boolean,     // степень важности ошибки:
                            // если true, то дальнейшая работа приложения не возможна, показываем диалог "переустановите или обратитесь к разработчику"
                            // false - показываем "попробуйте повторить операцию"
    val code: Int,          // код ошибки, см. ниже
    val desc: String,       // описание ошибки для пользователя
    val exMessage: String = "",  // сообщение в исключении
    val stackTrace: String = "" // stack trace исключения
) {
    companion object {
        val EMPTY = ErrorDescription(false, 0, "")
    }
}

/*  Кодирование кодов ошибок
    Положительные коды - системные ошибки с номером модуля и ошибки.
    Отрицательные коды - ошибки прикладного уровня, кодируются в каждом стеке самостоятельно (см. UIErrno)

    code = <модуль><номер ошибки в модуле>

    <модуль> ::= <digit><digit>     -- код модуля из двух цифр

    <номер ошибки в модуле> ::= <digit><digit>
        | номер обработчика ошибки в модуле
        | номер ошибки, если она идёт с нижнего уровня

    Зарегистрированные модули:

        ---- Репозитории, хранилища ---
        10 - SensorsRepository

        ---- Сетевые запросы ----
        50 -
 */

/**
 * Вспомогательная функция для кодирования кода ошибки
 */
fun errCode(module: Int, errorNumber: Int): Int {
    return module * 100 + errorNumber
}

/**
 * Возвращает форматированную строку со stack trace из исключения
 */
fun stackTraceToString(ex: Exception): String {
    val result = StringBuilder()

    ex.stackTrace.forEach {
        result.append(it.toString())
        result.append("\n")
    }

    return result.toString()
}