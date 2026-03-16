package org.entur.ror.ashur.sax.plugins.servicejourneynaming

/**
 * Parsing context that tracks the current state during SAX parsing.
 * Follows the pattern from ActiveDatesParsingContext.
 */
data class ServiceJourneyNameParsingContext(
    /** Current JourneyPattern being parsed (to associate with first stop's DestinationDisplayRef) */
    var currentJourneyPatternId: String? = null,

    /** Current ServiceJourney being parsed */
    var currentServiceJourneyId: String? = null,

    /** Current DestinationDisplay being parsed (to collect FrontText) */
    var currentDestinationDisplayId: String? = null,

    /** Whether we're inside the first StopPointInJourneyPattern (order="1") */
    var isFirstStopPoint: Boolean = false,

    /** Whether we've already found DestinationDisplayRef for current JourneyPattern */
    var foundDestinationDisplayRefForCurrentPattern: Boolean = false
)
