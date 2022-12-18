package datamodels

import oper.Utiler

import java.math.BigInteger

case class RawNewsDTO(
                       Categories: List[String],
                       Title: String,
                       Url: String,
                       PublishedDateTime: String
                     ) {
  def md5(): String = {
//    Utiler.md5(f"${this.Title}_${this.Url}_${this.PublishedDateTime}")
    Utiler.md5(f"${this.Title}_${this.Url}")
  }

}


