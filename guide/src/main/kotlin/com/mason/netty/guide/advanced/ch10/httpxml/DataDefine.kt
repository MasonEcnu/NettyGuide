package com.mason.netty.guide.advanced.ch10.httpxml

/**
 * Created by mwu on 2018/10/22
 * 没找到在idea和gradle下使用jibx的相关资料
 * 该章节跳过
 */
data class Order(val orderNumber: Long, val customer: Customer)

data class Customer(val customerNumber: Long, val firstName: String, val lastName: String, var middleNames: List<String> = emptyList())

data class Address(val street1: String, var street2: String = "", val city: String, val state: String, val country: String)

enum class Shipping {
  STANDARD_MAIL,
  PRIORITY_MAIL,
  INTERNATIONAL_MAIL,
  DOMESTIC_EXPRESS,
  INTERNATIONAL_EXPRESS
}