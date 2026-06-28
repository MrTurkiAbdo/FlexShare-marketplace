package com.example.flexshare.backend

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

// 📐 1. SQL Schema for Listings Table
object ListingsTable : Table("listings") {
    val id = varchar("id", 50)
    val title = varchar("title", 100)
    val description = varchar("description", 500)
    val pricePerDay = double("price_per_day")
    val ownerName = varchar("owner_name", 100)
    val category = varchar("category", 50)
    val condition = varchar("condition", 50)
    val imageUrl = varchar("image_url", 500).nullable()

    override val primaryKey = PrimaryKey(id)
}

// 📐 2. SQL Schema for Bookings Table (Persistent Rental History)
object BookingsTable : Table("bookings") {
    val id = varchar("id", 50)
    val listingId = varchar("listing_id", 50)
    val listingTitle = varchar("listing_title", 100)
    val renterName = varchar("renter_name", 100)
    val days = integer("days")
    val totalPrice = double("total_price")
    val bookingDate = varchar("booking_date", 50)

    override val primaryKey = PrimaryKey(id)
}

fun Application.module() {
    install(CallLogging)

    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            ignoreUnknownKeys = true
        })
    }

    // 🎯 CONNECT TO H2 DATABASE (Saves file named 'flexshare_db.mv.db' in your project root)
    Database.connect("jdbc:h2:./flexshare_db;DB_CLOSE_DELAY=-1;", driver = "org.h2.Driver")

    // ⚙️ Create tables automatically if they don't exist
    transaction {
        SchemaUtils.create(ListingsTable, BookingsTable)

        // Seed an initial item if the database is brand new and empty
        if (ListingsTable.selectAll().count() == 0L) {
            ListingsTable.insert {
                it[id] = UUID.randomUUID().toString()
                it[title] = "Makita Cordless Drill Set"
                it[description] = "Heavy-duty 18V drill with 2 batteries."
                it[pricePerDay] = 15.00
                it[ownerName] = "Ahmed Al-Mansour"
                it[category] = "Tools"
                it[condition] = "Good"
                it[imageUrl] = "https://images.unsplash.com/photo-1504148455328-c376907d081c?w=500"
            }
        }
    }

    routing {
        route("/v1") {

            // 📥 1. GET Listings from SQL Database
            get("/listings") {
                val listings = transaction {
                    ListingsTable.selectAll().map {
                        Listing(
                            id = it[ListingsTable.id],
                            title = it[ListingsTable.title],
                            description = it[ListingsTable.description],
                            pricePerDay = it[ListingsTable.pricePerDay],
                            ownerName = it[ListingsTable.ownerName],
                            category = it[ListingsTable.category],
                            condition = it[ListingsTable.condition],
                            imageUrl = it[ListingsTable.imageUrl]
                        )
                    }
                }
                call.respond(listings)
            }

            // 📤 2. POST Listing directly into SQL Database
            post("/listings") {
                try {
                    val incomingListing = call.receive<Listing>()
                    val serverGeneratedId = UUID.randomUUID().toString()

                    transaction {
                        ListingsTable.insert {
                            it[id] = serverGeneratedId
                            it[title] = incomingListing.title
                            it[description] = incomingListing.description
                            it[pricePerDay] = incomingListing.pricePerDay
                            it[ownerName] = incomingListing.ownerName
                            it[category] = incomingListing.category
                            it[condition] = incomingListing.condition
                            it[imageUrl] = incomingListing.imageUrl
                        }
                    }

                    val finalListing = incomingListing.copy(id = serverGeneratedId)
                    call.respond(HttpStatusCode.Created, finalListing)
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText(e.localizedMessage, status = HttpStatusCode.BadRequest)
                }
            }

            // 🤝 3. POST Rental Booking into SQL Database (Echoes matching payload back to Android)
            post("/bookings") {
                try {
                    val rawJsonPayload = call.receiveText()
                    println("Received booking payload: $rawJsonPayload")

                    // Parse JSON dynamically to extract details and save securely into our SQL schema
                    val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(rawJsonPayload)
                    val jsonObject = kotlinx.serialization.json.Json.decodeFromString<kotlinx.serialization.json.JsonObject>(rawJsonPayload)

                    val serverBookingId = UUID.randomUUID().toString()

                    transaction {
                        BookingsTable.insert {
                            it[id] = serverBookingId
                            it[listingId] = jsonObject["listingId"]?.toString()?.replace("\"", "") ?: ""
                            it[listingTitle] = jsonObject["listingTitle"]?.toString()?.replace("\"", "") ?: ""
                            it[renterName] = jsonObject["renterName"]?.toString()?.replace("\"", "") ?: ""
                            it[days] = jsonObject["days"]?.toString()?.toIntOrNull() ?: 1
                            it[totalPrice] = jsonObject["totalPrice"]?.toString()?.toDoubleOrNull() ?: 0.0
                            it[bookingDate] = jsonObject["bookingDate"]?.toString()?.replace("\"", "") ?: ""
                        }
                    }

                    // 🚀 Echo back the complete JSON object payload to satisfy Android's data parsing parser
                    call.respond(
                        status = HttpStatusCode.Created,
                        message = jsonElement
                    )

                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Failed to process booking: ${e.localizedMessage}", status = HttpStatusCode.BadRequest)
                }
            }
        }
    }
}