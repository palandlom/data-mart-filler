package oper

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import datamodels.RawNewsDTO

import java.net.URL
import scala.jdk.javaapi.CollectionConverters.asScala
import java.net.URL
import java.time.Instant
import scala.jdk.CollectionConverters._



class RawDataExtractor {

  def collectNews(urlStr: String): Seq[RawNewsDTO] = {

    val feedUrl = new URL(urlStr) // TODO handle exception
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl)) // TODO throws IllegalArgumentException, FeedException
    val entries = asScala(feed.getEntries).toVector


    entries.map(entry => {
      RawNewsDTO(entry.getCategories.asScala.map(sc => sc.getName).toList,
        entry.getTitle,
        entry.getUri,
        entry.getPublishedDate.toInstant.toString)}
    )
//
//
//    println("Date:  " + entry.getUpdatedDate)
//    NewsDTO(entry.getTitle, entry.getUri, _) // TODO entry.getUpdatedDate
//
//
//    for (entry <- entries) {
//      val news = NewsDTO(entry.getTitle, entry.getUri,_) // TODO entry.getUpdatedDate)
//      println("Date:  " + entry.getUpdatedDate)
//
//      // java.util.List[SyndLink]
//      val links = asScala(entry.getLinks).toVector
//      for (link <- links) {
//        println("Link: " + link.getHref)
//      }
//
//      val contents = asScala(entry.getContents).toVector
//      for (content <- contents) {
//        println("Content: " + content.getValue)
//      }
//
//      val categories = asScala(entry.getCategories).toVector
//      for (category <- categories) {
//        println("Category: " + category.getName)
//      }
//
//      println("")
//    }

  }

}

