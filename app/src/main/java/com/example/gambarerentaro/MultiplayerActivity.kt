package com.example.gambarerentaro

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.gambarerentaro.ui.theme.GambareRentaroTheme
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy

class MultiplayerActivity : ComponentActivity() {

    private lateinit var nickname: String
    private val serviceId = "com.example.gambarerentaro"
    private var opponentEndpointId: String? = null // opponentEndpointId を宣言
    private var opponentName: String? = null
    private var connectionInfo: ConnectionInfo? = null

    private lateinit var connectionsClient: ConnectionsClient
    private val endpointId: String? = null // 接続先のエンドポイントID
    private val REQUEST_CODE_REQUIRED_PERMISSIONS = 1
    private lateinit var btn_find_opponent: Button
    private lateinit var tv_opponent_name: TextView
    private lateinit var btn_start_game: Button

    // ... (既存のコード) ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multiplayer)

        nickname = intent.getStringExtra("NICKNAME") ?: "You" // 名前を取得、なければデフォルト値

        connectionsClient = Nearby.getConnectionsClient(this)

        // 必要なパーミッションを確認
        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS)
        }

        btn_find_opponent = findViewById(R.id.btn_find_opponent)
        tv_opponent_name = findViewById(R.id.tv_opponent_name)
        btn_start_game = findViewById(R.id.btn_start_game)
        btn_find_opponent.setOnClickListener {
            startAdvertising()
            startDiscovery()
        }
    }// ... (既存のコード) ...

    /** Returns true if permissions are granted. */
    private fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    /** Handles user acceptance/rejection of our location and storage permissions. */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode != REQUEST_CODE_REQUIRED_PERMISSIONS) {
            return
        }
        for (grantResult in grantResults) {
            if (grantResult == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "パーミッションが必要です", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        recreate()
    }

    companion object {
        private const val TAG = "MultiplayerActivity"

        private val STRATEGY = Strategy.P2P_STAR

        // パーミッションの要求
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    private fun startAdvertising() {
        connectionsClient.startAdvertising(
            nickname, //デバイスのニックネーム
            serviceId, // サービスID
            connectionLifecycleCallback, // 接続ライフサイクルコールバック
            AdvertisingOptions.Builder().setStrategy(STRATEGY).build() // 広告オプション
        )
            .addOnSuccessListener {
                Log.d(TAG, "広告を開始しました")
                // 広告が開始されたら、UI を更新するなど
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "広告の開始に失敗しました", e)
                // 広告の開始に失敗したら、エラー処理を行うなど
            }
    }

    private fun startDiscovery() {
        connectionsClient.startDiscovery(
            serviceId, // サービスID
            endpointDiscoveryCallback, // エンドポイント検出コールバック
            DiscoveryOptions.Builder().setStrategy(STRATEGY).build() // 検出オプション
        )
            .addOnSuccessListener {
                Log.d(TAG, "検出を開始しました")
                // 検出が開始されたら、UI を更新するなど
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "検出の開始に失敗しました", e)
                // 検出の開始に失敗したら、エラー処理を行うなど
            }
    }

    private val connectionLifecycleCallback = object : ConnectionLifecycleCallback() {
        override fun onConnectionInitiated(endpointId:String, connectionInfo: ConnectionInfo) {
            // 接続が開始されたときの処理
            this@MultiplayerActivity.connectionInfo = connectionInfo // connectionInfo を保存
            connectionsClient.acceptConnection(endpointId, payloadCallback)
            opponentName = connectionInfo.endpointName // エンドポイント名を取得
            runOnUiThread {
                tv_opponent_name.text = "対戦相手:$opponentName"
            }
        }

        override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
            // 接続結果を受け取ったときの処理
            when (result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    // 接続が開始されたときの処理
                    connectionsClient.acceptConnection(endpointId, payloadCallback)
                    opponentName = connectionInfo?.endpointName // エンドポイント名を取得
                    runOnUiThread {
                        tv_opponent_name.text = "対戦相手:$opponentName"
                    }
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    // 接続が拒否されたときの処理
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    // 接続エラーが発生したときの処理
                }
            }
        }

        override fun onDisconnected(endpointId: String) {
            // 接続が切断されたときの処理
            if (endpointId == opponentEndpointId) {
                opponentEndpointId = null
                opponentName = null
                runOnUiThread {
                    tv_opponent_name.text = "対戦相手: "
                    btn_start_game.isEnabled = false
                }
            }
        }
    }

    private val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointId: String, payload: Payload) {
            // ペイロードを受信したときの処理
            when (payload.type) {
                Payload.Type.BYTES -> {
                    val receivedBytes = payload.asBytes()!!
                    val receivedMessage = String(receivedBytes, Charsets.UTF_8)
                    // 受信したメッセージを処理
                    Log.d(TAG, "受信したメッセージ: $receivedMessage")
                    // ...
                }
                // ... 他のペイロードタイプを処理 ...
                else -> {
                    // 不明なペイロードタイプ
                }
            }
        }

        override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
            // ペイロード転送の更新を受け取ったときの処理
            // ... 必要に応じて処理を追加 ...
        }
    }

    private val endpointDiscoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            // エンドポイントが検出されたときの処理
            Log.i(TAG, "エンドポイントを検出しました: $endpointId")
            // ... 他の処理 ...
            connectionsClient.requestConnection(nickname, endpointId, connectionLifecycleCallback)
                .addOnSuccessListener { unused: Void? ->
                    // Wesuccessfully requested a connection. Now both sides
                    // must accept before the connection is established.
                }
                .addOnFailureListener { e: Exception? ->
                    // Nearby Connections failed to request the connection.
                }
        }

        override fun onEndpointLost(endpointId: String) {
            // エンドポイントが失われたときの処理
            Log.i(TAG, "エンドポイントを失いました: $endpointId")
            // ... 他の処理 ...
        }
    }
}

@Composable
fun Greeting3(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview3() {
    GambareRentaroTheme {
        Greeting3("Android")
    }
}