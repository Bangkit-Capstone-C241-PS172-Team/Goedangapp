package com.example.goedangapp.retrofit

import com.example.goedangapp.response.AddItemResponse
import com.example.goedangapp.response.AuthResponse
import com.example.goedangapp.response.ItemDetailResponse
import com.example.goedangapp.response.ItemEntryItem
import com.example.goedangapp.response.ItemEntryResponse
import com.example.goedangapp.response.ItemLastEntryResponse
import com.example.goedangapp.response.ItemLastEntryResponseItem
import com.example.goedangapp.response.ItemResponseItem
import com.example.goedangapp.response.LoginResponse2
import com.example.goedangapp.response.SalesOverviewResponse
import com.example.goedangapp.response.SalesOverviewResponseItem
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {


    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("full_name") full_name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): AuthResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse2

    @FormUrlEncoded
    @POST("items")
    suspend fun addItem(
        @Field("measuring_unit") measuringUnit: String,
        @Field("name") name: String,
        @Field("quantity") quantity: Int,
        @Field("user_id") userId : String,
    ) : AddItemResponse

    @GET( "items")
    suspend fun getItem(): List<ItemResponseItem>

    @GET("items/{id}")
    suspend fun getItemById(
        @Path("id") id: String
    ): ItemDetailResponse
    @GET("item_entries/item/{id}")
    suspend fun getItemEntriesById(
        @Path("id") id: String
    ): List<ItemEntryItem>


    @FormUrlEncoded
    @POST("item_entries")
    suspend fun addItemEntry(
        @Field("item_id") itemId: String,
        @Field("user_id") userId: String,
        @Field("in_out") inOut: String,
        @Field("quantity") quantity: Int,
        @Field("price") price: Int,
        @Field("total") total: Int,
    ) : ItemEntryResponse

    @GET( "item_entries")
    suspend fun getItemEntries(): List<ItemEntryItem>

    @GET("items_with_last_entry")
    suspend fun getItemLastEntries(): List<ItemLastEntryResponseItem>

    @GET("sales_overview")
    suspend fun getSalesOverview(): SalesOverviewResponse


}