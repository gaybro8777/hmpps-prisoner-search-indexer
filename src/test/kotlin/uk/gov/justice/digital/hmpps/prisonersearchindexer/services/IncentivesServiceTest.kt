package uk.gov.justice.digital.hmpps.prisonersearchindexer.services

import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.prisonersearchindexer.config.WebClientConfiguration
import uk.gov.justice.digital.hmpps.prisonersearchindexer.integration.wiremock.IncentivesApiExtension.Companion.incentivesApi
import java.time.LocalDate
import java.time.LocalDateTime

@SpringAPIServiceTest
@Import(IncentivesService::class, WebClientConfiguration::class)
internal class IncentivesServiceTest {
  @Autowired
  private lateinit var incentivesService: IncentivesService

  @Nested
  @DisplayName("currentIncentive")
  inner class IncentiveLevel {
    @BeforeEach
    internal fun setUp() {
      incentivesApi.stubCurrentIncentive()
    }

    @Test
    internal fun `will supply authentication token`() {
      incentivesService.getCurrentIncentive(123456L)

      incentivesApi.verify(
        getRequestedFor(urlEqualTo("/iep/reviews/booking/123456?with-details=false"))
          .withHeader("Authorization", equalTo("Bearer ABCDE")),

      )
    }

    @Test
    internal fun `will return current incentive`() {
      incentivesApi.stubCurrentIncentive(
        iepCode = "STD",
        iepLevel = "Standard",
        iepTime = "2022-11-10T15:47:24.682335",
        nextReviewDate = "2023-11-18",
      )

      val incentive = incentivesService.getCurrentIncentive(123456L)!!
      incentivesApi.verifyGetCurrentIncentiveRequest(123456L)

      assertThat(incentive.iepCode).isEqualTo("STD")
      assertThat(incentive.iepLevel).isEqualTo("Standard")
      assertThat(incentive.iepTime).isEqualTo(LocalDateTime.parse("2022-11-10T15:47:24.682335"))
      assertThat(incentive.nextReviewDate).isEqualTo(LocalDate.parse("2023-11-18"))
    }
  }
}
