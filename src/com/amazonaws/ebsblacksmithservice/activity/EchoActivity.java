
package com.amazonaws.ebsblacksmithservice.activity;

import com.amazon.coral.annotation.Service;
import com.amazonaws.ebsblacksmithservice.AbstractEchoActivity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Activity class for all echo-related methods.
 *
 */
@Service("EbsBlacksmithService")
public class EchoActivity extends AbstractEchoActivity {

    private static final Logger log = LoggerFactory.getLogger(EchoActivity.class);

    /**
     * Does nothing of value, but at least one method must exist for Coral
     * Explorer to work.
     *
     */
    @Override
    public void enact() {
        log.info("Echo called");
    }
}
