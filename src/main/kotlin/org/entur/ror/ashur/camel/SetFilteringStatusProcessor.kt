package org.entur.ror.ashur.camel

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.entur.ror.ashur.Constants
import org.entur.ror.ashur.addPubsubAttribute

/**
 * SetFilteringStatusProcessor is a Camel processor that sets the filtering status
 * in the Pub/Sub message attributes of the exchange.
 *
 * @param status The filtering status to set.
 */
class SetFilteringStatusProcessor(val status: String): Processor {
    override fun process(exchange: Exchange) {
        exchange.addPubsubAttribute(Constants.FILTERING_REPORT_STATUS_HEADER, status)
        exchange.getIn()
            .getHeader(Constants.FILTERING_PROFILE_HEADER, String::class.java)
            ?.let { exchange.addPubsubAttribute(Constants.FILTERING_PROFILE_HEADER, it) }
    }
}