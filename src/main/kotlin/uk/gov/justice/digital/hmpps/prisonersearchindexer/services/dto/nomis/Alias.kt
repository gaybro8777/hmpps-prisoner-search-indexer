package uk.gov.justice.digital.hmpps.prisonersearchindexer.services.dto.nomis

import java.time.LocalDate

/**
 * Alias
 */
data class Alias(
  val firstName: String,
  val middleName: String?,
  val lastName: String,
  val age: Int?,
  val dob: LocalDate,
  val gender: String?,
  val ethnicity: String?,
  val nameType: String?,
  val createDate: LocalDate,
)
