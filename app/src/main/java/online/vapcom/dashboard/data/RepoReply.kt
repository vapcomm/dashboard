/*
 * (c) VAP Communications Group, 2020
 */


package online.vapcom.dashboard.data

/**
 * Ответы от репозитория
 */
sealed class RepoReply {

    /**
     * Ошибка при обработке запроса
     */
    class Error(val error: ErrorDescription) : RepoReply() {
        override fun toString(): String {
            return "RepoReply: Error: $error"
        }
    }

    /**
     * Операция завершена успешно
     */
    object Success : RepoReply()
}
