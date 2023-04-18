package OMS_API


import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._


import scala.language.postfixOps

class OMSAPITest extends Simulation{

  private val host: String = System.getProperty("urlCible", "http://vip-gl-pp-oms-worker.pp.ecom.inet:9318")
  private val VersionAppli: String = System.getProperty("VersionApp", "Vxx.xx.xx")
  private val TpsMonteEnCharge: Int = System.getProperty("tpsMonte", "15").toInt
  private val TpsPalier: Int = System.getProperty("tpsPalier", (2 * TpsMonteEnCharge).toString).toInt
  private val TpsPause: Int = System.getProperty("tpsPause", "60").toInt
  private val DureeMax: Int = System.getProperty("dureeMax", "1").toInt + 5 * (TpsMonteEnCharge + TpsPalier)

  private val tpsPaceDefault: Int = System.getProperty("tpsPace", "1000").toInt
  private val tpsPacingProducts: Int = System.getProperty("tpsPaceProducts", tpsPaceDefault.toString).toInt

  private val LeCoeff: Int = System.getProperty("coeff", "10").toInt
  private val  nbVu : Int =  LeCoeff * 1

  private val FichierPath: String = System.getProperty("dataDir", "data/")
  private val FichierDataCustomerId: String = "JddCustomerId.csv"
  private val FichierDataOrderId: String = "JddOrderId.csv"

  val jddDataCustomerId = csv(FichierPath + FichierDataCustomerId).circular

  val jddDataOrderId = csv(FichierPath + FichierDataOrderId).circular


  val httpProtocol =   http
    .baseUrl(host)
    .acceptHeader("application/json")


  before {

    println("----------------------------------------------" )
    println("host :"+ host   )
    println("VersionAppli :"+ VersionAppli   )
    println("TpsPause : " + TpsPause  )
    println("LeCoeff : " + LeCoeff  )
    println("nbVu : " + nbVu  )
    println("tpsMonte : " + TpsMonteEnCharge )
    println("----------------------------------------------" )
  }

  after  {
    println("----------------------------------------------" )
    println("--------     Rappel - Rappel - Rappel    -----" )
    println("VersionAppli :"+ VersionAppli   )
    println("host :"+ host   )
    println("TpsPause : " + TpsPause  )
    println("LeCoeff : " + LeCoeff  )
    println("nbVu : " + nbVu  )
    println("DureeMax : " + DureeMax )
    println("tpsMonte : " + TpsMonteEnCharge )
    println("--------     Rappel - Rappel - Rappel    -----" )
    println("----------------------------------------------" )
    println(" " )
  }


////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  historique de commandes /////////////////////
  /////////////////////////////////////////////////////////////////////////////


  val omsHistoriqueCommandes = scenario("OMS - Historique des commandes")
    .exec(flushSessionCookies)
    .exec(flushHttpCache)
    .exec(flushCookieJar)
    .pace(tpsPacingProducts milliseconds)
    .feed(jddDataCustomerId)
    .exec { session =>
      println("CustomerID :" + session("customerId").as[String])
      session
    }
    .exec(http("historiqueCommandes")
      .get("/oms-api/rest/v1/orders/commercials/query?q=customer.customerIdRef==${customerId};saleInfo.originType!=SHOP")
      .check(status.is(200)))




  ////////////////////////////////////////////////////////////////////////////
  ///////////////////////////////  Détails de commandes /////////////////////
  /////////////////////////////////////////////////////////////////////////////

  val omsDetailCommande = scenario("OMS - Détail de commande")
    .exec(flushSessionCookies)
    .exec(flushHttpCache)
    .exec(flushCookieJar)
    .pace(tpsPacingProducts milliseconds)
    .feed(jddDataOrderId)
    .exec { session =>
      println("OrderID :" + session("orderId").as[String])
      session
    }
    .exec(http("detailCommande")
      .get("/oms-api/rest/v1/orders/commercials/query?q=code==${orderId}")
      .check(status.is(200)))
 

  setUp(
    omsHistoriqueCommandes.inject(rampUsers(nbVu * 10) during ( TpsMonteEnCharge  minutes) , nothingFor(  TpsPalier  minutes)),
    omsDetailCommande.inject(rampUsers(nbVu * 10) during ( TpsMonteEnCharge  minutes) , nothingFor(  TpsPalier  minutes)),
  ).protocols(httpProtocol)
    .maxDuration( DureeMax minutes)

}
