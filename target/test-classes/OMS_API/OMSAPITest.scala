package OMS_API


import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._


import scala.language.postfixOps

class OMSAPITest extends Simulation{

  private val host: String = System.getProperty("urlCible", "http://ws-oms.galerieslafayette.com")
  private val VersionAppli: String = System.getProperty("VersionApp", "Vxx.xx.xx")
  private val TpsMonteEnCharge: Int = System.getProperty("tpsMonte", "15").toInt
  private val TpsPalier: Int = System.getProperty("tpsPalier", (2 * TpsMonteEnCharge).toString).toInt
  private val TpsPause: Int = System.getProperty("tpsPause", "60").toInt
  private val DureeMax: Int = System.getProperty("dureeMax", "1").toInt + 5 * (TpsMonteEnCharge + TpsPalier)

  private val LeCoeff: Int = System.getProperty("coeff", "10").toInt
  private val  nbVu : Int =  LeCoeff * 1


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



  val omsHistoriqueCommandes = scenario("OMS - Historique des commandes")
    .exec(http("historiqueCommandes")
      .get("/oms-api/rest/v1/orders/commercials/query?q=customer.customerIdRef=={{customerId}}")
      .check(status.is(200)))

  val omsDetailCommande = scenario("OMS - Détail de commande")
    .exec(http("detailCommande")
      .get("/oms-api/rest/v1/orders/commercials/query?q=code=={{orderId}}")
      .check(status.is(200)))
 

  setUp(
    omsHistoriqueCommandes.inject(rampUsers(nbVu * 10) during ( TpsMonteEnCharge  minutes) , nothingFor(  TpsPalier  minutes)),
    omsDetailCommande.inject(rampUsers(nbVu * 10) during ( TpsMonteEnCharge  minutes) , nothingFor(  TpsPalier  minutes)),
  ).protocols(httpProtocol)
    .maxDuration( DureeMax minutes)

}
