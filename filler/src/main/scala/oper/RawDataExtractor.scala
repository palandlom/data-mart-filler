package oper

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import datamodels.RawNewsDTO

import java.net.URL
import scala.jdk.javaapi.CollectionConverters.asScala
import java.net.URL
import java.time.Instant
import scala.jdk.CollectionConverters._


/** Collect new from rss-sources */
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
        entry.getPublishedDate.toInstant.toString)
    }
    )
  }

}

