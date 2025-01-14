package uk.gov.justice.digital.hmpps.prisonersearchindexer.services

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class IncentivesService(
  private val incentivesWebClient: WebClient,
  @Value("\${api.incentives.timeout:20s}") private val timeout: Duration,
) {
  fun getCurrentIncentive(bookingId: Long): IncentiveLevel? =
    incentivesWebClient.get().uri("/iep/reviews/booking/{bookingId}?with-details=false", bookingId)
      .retrieve()
      .bodyToMono(IncentiveLevel::class.java)
      .onErrorResume(WebClientResponseException.NotFound::class.java) {
        Mono.empty()
      }
      .block(timeout)
}

data class IncentiveLevel(
  val iepCode: String,
  val iepLevel: String,
  val iepTime: LocalDateTime,
  val nextReviewDate: LocalDate?,
)
