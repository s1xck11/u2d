package com.example.ebikedisplay

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "WebServer"

class WebServer(
    port: Int = 8080,
    private val stateProvider: () -> DashboardState,
    private val tripsProvider: () -> List<Trip>
) : NanoHTTPD(port) {

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri ?: "/"
        Log.d(TAG, "Запрос: $uri")

        return when {
            uri == "/" -> serveHtml()
            uri == "/api/state" -> serveState()
            uri == "/api/trips" -> serveTrips()
            else -> newFixedLengthResponse(
                Response.Status.NOT_FOUND,
                MIME_PLAINTEXT,
                "Not found"
            )
        }
    }

    private fun serveHtml(): Response {
        val html = """
            <!DOCTYPE html>
            <html lang="ru">
            <head>
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>EBike Control Panel</title>
                <style>
                    body {
                        background: #0D0D0D;
                        color: white;
                        font-family: -apple-system, sans-serif;
                        margin: 0;
                        padding: 20px;
                    }
                    h1 { color: #00E5FF; }
                    .card {
                        background: #1A1A1A;
                        border-radius: 12px;
                        padding: 16px;
                        margin: 12px 0;
                    }
                    .row { display: flex; justify-content: space-between; padding: 6px 0; }
                    .label { color: #888; font-size: 14px; }
                    .value { font-weight: bold; font-size: 18px; }
                </style>
            </head>
            <body>
                <h1>EBike Control Panel</h1>
                <div class="card" id="live">
                    <div class="row"><span class="label">Скорость</span><span class="value" id="speed">—</span></div>
                    <div class="row"><span class="label">Напряжение</span><span class="value" id="voltage">—</span></div>
                    <div class="row"><span class="label">Батарея</span><span class="value" id="battery">—</span></div>
                    <div class="row"><span class="label">Запас хода</span><span class="value" id="range">—</span></div>
                    <div class="row"><span class="label">Режим</span><span class="value" id="mode">—</span></div>
                    <div class="row"><span class="label">Мощность</span><span class="value" id="power">—</span></div>
                    <div class="row"><span class="label">Температура</span><span class="value" id="temp">—</span></div>
                    <div class="row"><span class="label">Поворотники</span><span class="value" id="turn">—</span></div>
                </div>

                <h2>История поездок</h2>
                <div class="card" id="trips">Загрузка...</div>

                <script>
                    async function update() {
                        try {
                            const r = await fetch('/api/state');
                            const d = await r.json();
                            document.getElementById('speed').textContent = d.speed.toFixed(1) + ' km/h';
                            document.getElementById('voltage').textContent = d.voltage.toFixed(1) + ' V';
                            document.getElementById('battery').textContent = d.battery + ' %';
                            document.getElementById('range').textContent = d.range + ' km';
                            document.getElementById('mode').textContent = d.speedMode;
                            document.getElementById('power').textContent = d.power.toFixed(0) + ' W';
                            document.getElementById('temp').textContent = d.temperature + ' °C';
                            let turn = '';
                            if (d.leftTurn) turn += '◀ ';
                            if (d.rightTurn) turn += '▶';
                            if (turn === '') turn = '—';
                            document.getElementById('turn').textContent = turn;
                        } catch (e) {
                            console.error(e);
                        }
                    }
                    setInterval(update, 1000);
                    update();

                    async function loadTrips() {
                        try {
                            const r = await fetch('/api/trips');
                            const trips = await r.json();
                            if (trips.length === 0) {
                                document.getElementById('trips').textContent = 'Пока нет поездок';
                                return;
                            }
                            let html = '';
                            for (const t of trips) {
                                html += '<div class="row"><span class="label">' + t.date + '</span>' +
                                    '<span class="value">' + t.distance.toFixed(2) + ' km, ' +
                                    t.whUsed.toFixed(1) + ' Wh</span></div>';
                            }
                            document.getElementById('trips').innerHTML = html;
                        } catch (e) {
                            console.error(e);
                        }
                    }
                    loadTrips();
                    setInterval(loadTrips, 10000);
                </script>
            </body>
            </html>
        """.trimIndent()

        return newFixedLengthResponse(
            Response.Status.OK,
            "text/html; charset=utf-8",
            html
        )
    }

    private fun serveState(): Response {
        val s = stateProvider()
        val json = JSONObject().apply {
            put("speed", s.speed)
            put("voltage", s.voltage)
            put("battery", s.battery)
            put("range", s.range)
            put("temperature", s.temperature)
            put("power", s.power)
            put("consumption", s.consumption)
            put("speedMode", s.speedMode)
            put("leftTurn", s.leftTurn)
            put("rightTurn", s.rightTurn)
            put("powered", s.powered)
            put("bleConnected", s.bleConnected)
        }
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            json.toString()
        )
    }

    private fun serveTrips(): Response {
        val trips = tripsProvider()
        val arr = JSONArray()
        trips.forEach { t ->
            arr.put(JSONObject().apply {
                put("date", t.date)
                put("distance", t.distance)
                put("whUsed", t.whUsed)
                put("avgSpeed", t.avgSpeed)
            })
        }
        return newFixedLengthResponse(
            Response.Status.OK,
            "application/json",
            arr.toString()
        )
    }
}