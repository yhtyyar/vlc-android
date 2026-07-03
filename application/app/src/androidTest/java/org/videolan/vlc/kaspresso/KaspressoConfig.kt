package org.videolan.vlc.kaspresso

import com.kaspersky.components.alluresupport.withAllureSupport
import com.kaspersky.kaspresso.kaspresso.Kaspresso

object KaspressoConfig {

    val builder: Kaspresso.Builder = Kaspresso.Builder.withAllureSupport()
}
