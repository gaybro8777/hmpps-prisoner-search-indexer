@file:Suppress("ktlint:filename")

package uk.gov.justice.digital.hmpps.prisonersearchindexer.model

import uk.gov.justice.digital.hmpps.prisonersearchindexer.services.IncentiveLevel
import uk.gov.justice.digital.hmpps.prisonersearchindexer.services.RestrictedPatient
import uk.gov.justice.digital.hmpps.prisonersearchindexer.services.canonicalPNCNumberLong
import uk.gov.justice.digital.hmpps.prisonersearchindexer.services.canonicalPNCNumberShort
import uk.gov.justice.digital.hmpps.prisonersearchindexer.services.dto.nomis.OffenderBooking

fun Prisoner.translate(existingPrisoner: Prisoner? = null, ob: OffenderBooking, incentiveLevel: Result<IncentiveLevel?>, restrictedPatientData: RestrictedPatient?): Prisoner {
  this.prisonerNumber = ob.offenderNo
  this.bookNumber = ob.bookingNo
  this.bookingId = ob.bookingId?.toString()
  this.pncNumber = ob.identifiers?.firstOrNull { i -> i.type == "PNC" }?.value
  this.pncNumberCanonicalShort =
    ob.identifiers?.firstOrNull { i -> i.type == "PNC" }?.value?.canonicalPNCNumberShort()
  this.pncNumberCanonicalLong =
    ob.identifiers?.firstOrNull { i -> i.type == "PNC" }?.value?.canonicalPNCNumberLong()
  this.croNumber = ob.identifiers?.firstOrNull { i -> i.type == "CRO" }?.value

  this.cellLocation = ob.assignedLivingUnit?.description
  this.prisonName = ob.assignedLivingUnit?.agencyName
  this.prisonId = ob.agencyId
  this.status = ob.status
  this.inOutStatus = ob.inOutStatus
  this.lastMovementTypeCode = ob.lastMovementTypeCode
  this.lastMovementReasonCode = ob.lastMovementReasonCode

  this.category = ob.categoryCode
  this.csra = ob.csra

  this.dateOfBirth = ob.dateOfBirth
  this.firstName = ob.firstName
  this.middleNames = ob.middleName
  this.lastName = ob.lastName

  this.aliases =
    ob.aliases?.map { a -> PrisonerAlias(a.firstName, a.middleName, a.lastName, a.dob, a.gender, a.ethnicity) }
  this.alerts =
    ob.alerts?.filter { a -> a.active }?.map { a -> PrisonerAlert(a.alertType, a.alertCode, a.active, a.expired) }

  this.gender = ob.physicalAttributes?.gender
  this.ethnicity = ob.physicalAttributes?.ethnicity
  this.heightCentimetres = ob.physicalAttributes?.heightCentimetres
  this.weightKilograms = ob.physicalAttributes?.weightKilograms

  ob.physicalCharacteristics?.filterNot { it.detail.isNullOrBlank() }?.forEach {
    when (it.type) {
      "HAIR" -> this.hairColour = it.detail
      "R_EYE_C" -> this.rightEyeColour = it.detail
      "L_EYE_C" -> this.leftEyeColour = it.detail
      "FACIAL_HAIR" -> this.facialHair = it.detail
      "FACE" -> this.shapeOfFace = it.detail
      "BUILD" -> this.build = it.detail
      "SHOESIZE" -> this.shoeSize = it.detail?.toIntOrNull()
    }
  }
  ob.physicalMarks?.forEach { pm ->
    pm.bodyPart?.let { BodyPartDetail(it, pm.comment) }?.let { bodyPart ->
      when (pm.type) {
        "Tattoo" -> this.tattoos = this.tattoos?.plus(bodyPart) ?: listOf(bodyPart)
        "Scar" -> this.scars = this.scars?.plus(bodyPart) ?: listOf(bodyPart)
        "Mark" -> this.marks = this.marks?.plus(bodyPart) ?: listOf(bodyPart)
        "Other" -> this.otherMarks = this.otherMarks?.plus(bodyPart) ?: listOf(bodyPart)
      }
    }
  }

  this.nationality = ob.profileInformation?.firstOrNull { p -> p.type == "NAT" }?.resultValue
  this.religion = ob.profileInformation?.firstOrNull { p -> p.type == "RELF" }?.resultValue
  this.maritalStatus = ob.profileInformation?.firstOrNull { p -> p.type == "MARITAL" }?.resultValue
  this.youthOffender =
    ob.profileInformation?.firstOrNull { p -> p.type == "YOUTH" }?.resultValue?.uppercase() == "YES"

  this.sentenceStartDate = ob.sentenceDetail?.sentenceStartDate
  this.confirmedReleaseDate = ob.sentenceDetail?.confirmedReleaseDate
  this.releaseDate = ob.sentenceDetail?.releaseDate
  this.sentenceExpiryDate = ob.sentenceDetail?.sentenceExpiryDate
  this.licenceExpiryDate = ob.sentenceDetail?.licenceExpiryDate
  this.homeDetentionCurfewEligibilityDate = ob.sentenceDetail?.homeDetentionCurfewEligibilityDate
  this.homeDetentionCurfewActualDate = ob.sentenceDetail?.homeDetentionCurfewActualDate
  this.homeDetentionCurfewEndDate = ob.sentenceDetail?.homeDetentionCurfewEndDate
  this.topupSupervisionStartDate = ob.sentenceDetail?.topupSupervisionStartDate
  this.topupSupervisionExpiryDate = ob.sentenceDetail?.topupSupervisionExpiryDate
  this.additionalDaysAwarded = ob.sentenceDetail?.additionalDaysAwarded
  this.nonDtoReleaseDate = ob.sentenceDetail?.nonDtoReleaseDate
  this.nonDtoReleaseDateType = ob.sentenceDetail?.nonDtoReleaseDateType
  this.tariffDate = ob.sentenceDetail?.tariffDate

  this.receptionDate = ob.receptionDate
  this.paroleEligibilityDate = ob.sentenceDetail?.paroleEligibilityDate
  this.automaticReleaseDate =
    ob.sentenceDetail?.automaticReleaseOverrideDate ?: ob.sentenceDetail?.automaticReleaseDate
  this.postRecallReleaseDate =
    ob.sentenceDetail?.postRecallReleaseOverrideDate ?: ob.sentenceDetail?.postRecallReleaseDate
  this.conditionalReleaseDate =
    ob.sentenceDetail?.conditionalReleaseOverrideDate ?: ob.sentenceDetail?.conditionalReleaseDate
  this.actualParoleDate = ob.sentenceDetail?.actualParoleDate

  this.locationDescription = restrictedPatientData
    ?.let { "${ob.locationDescription} - discharged to ${it.dischargedHospital?.description}" }
    ?: ob.locationDescription
  // get the most serious offence for this booking
  this.mostSeriousOffence =
    ob.offenceHistory?.firstOrNull { off -> off.mostSerious && off.bookingId == ob.bookingId }?.offenceDescription
  this.recall = ob.recall
  this.legalStatus = ob.legalStatus
  this.imprisonmentStatus = ob.imprisonmentStatus
  this.imprisonmentStatusDescription = ob.imprisonmentStatusDescription
  this.indeterminateSentence = ob.sentenceTerms?.any { st -> st.lifeSentence && st.bookingId == ob.bookingId }

  this.restrictedPatient = restrictedPatientData != null
  this.supportingPrisonId = restrictedPatientData?.supportingPrisonId
  this.dischargedHospitalId = restrictedPatientData?.dischargedHospital?.agencyId
  this.dischargedHospitalDescription = restrictedPatientData?.dischargedHospital?.description
  this.dischargeDate = restrictedPatientData?.dischargeDate
  this.dischargeDetails = restrictedPatientData?.dischargeDetails

  this.currentIncentive = incentiveLevel.map { it.toCurrentIncentive() }.getOrElse { existingPrisoner?.currentIncentive }

  return this
}

private fun IncentiveLevel?.toCurrentIncentive(): CurrentIncentive? = this?.let {
  CurrentIncentive(
    level = IncentiveLevel(it.iepCode, it.iepLevel),
    nextReviewDate = it.nextReviewDate,
    dateTime = it.iepTime.withNano(0), // ES only stores to the second
  )
}
