package org.rainrental.rainrentalrfid

import com.rscja.deviceapi.RFIDWithUHFUART
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import org.rainrental.rainrentalrfid.chainway.data.ChainwayRfidManager

class ChainwayRfidManagerTest {

    @Test
    fun `initializeHardware should set connectionStatus to true on successful init`() = runTest {
        // Mock the static method getInstance() of RFIDWithUHFUART
        val mockRfid = mockk<RFIDWithUHFUART>()
        mockkStatic(RFIDWithUHFUART::class)
        every { RFIDWithUHFUART.getInstance() } returns mockRfid

        // Stub the methods on the RFIDWithUHFUART instance
        every { mockRfid.init() } returns true
        every { mockRfid.setEPCAndTIDMode() } returns true
        every { mockRfid.setPower(any()) } returns true

        // Call the method under test
        ChainwayRfidManager.initializeHardware()

        // Assert connection status
        assertTrue("Connection status should be true", ChainwayRfidManager.getConnectionStatus().value)

        // Verify method calls on the mocked instance
//        verify { mockRfid.init() }
//        verify { mockRfid.setEPCAndTIDMode() }
//        verify { mockRfid.setPower(24) }

        // Cleanup
        unmockkStatic(RFIDWithUHFUART::class)
    }

    @Test
    fun `initializeHardware should set connectionStatus to false on failure`() = runTest {
        // Mock the static method getInstance() of RFIDWithUHFUART
        val mockRfid = mockk<RFIDWithUHFUART>()
        mockkStatic(RFIDWithUHFUART::class)
        every { RFIDWithUHFUART.getInstance() } returns mockRfid

        // Stub methods to simulate failure
        every { mockRfid.init() } returns false

        // Call the method under test
        ChainwayRfidManager.initializeHardware()

        // Assert connection status
        assertFalse("Connection status should be false", ChainwayRfidManager.getConnectionStatus().value)

        // Verify method calls on the mocked instance
        verify { mockRfid.init() }

        // Cleanup
        unmockkStatic(RFIDWithUHFUART::class)
    }
}