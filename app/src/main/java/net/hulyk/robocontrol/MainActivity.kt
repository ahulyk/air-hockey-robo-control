package net.hulyk.robocontrol

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    private val job = Job()

    private val socket = DatagramSocket()
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        joyStick.setOnMoveListener { angle, strength ->

            launch {
                val (dx, dy) = calculateDxDy(angle, strength)
                sendCoordinates(dx, dy)
                withContext(Main) {
                    logTxt.text = "angle:$angle, strength:$strength \ndx:$dx, dy:$dy"
                }
            }

        }

    }

    private fun calculateDxDy(angle: Int, strength: Int): Pair<Int, Int> {
        val theta = Math.toRadians(angle.toDouble())
        val multiplayer = seekBar.progress + 1
        val dx = -(strength * Math.cos(theta)).toInt() * multiplayer
        val dy = (strength * Math.sin(theta)).toInt() * multiplayer
        return Pair(dx, dy)
    }

    private fun sendCoordinates(dx: Int, dy: Int) {
        try {
            socket.connect(InetAddress.getByName(ipEdit.text.toString()), PORT)
            val payload = gson.toJson(Vector(dx, dy)).toByteArray()
            val packet = DatagramPacket(payload, payload.size)
            socket.send(packet)
        } catch (e: Exception) {
            logError.text = e.localizedMessage
            e.printStackTrace()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Default

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        socket.close()
    }

    companion object {
        private val PORT = 8008
    }

}
