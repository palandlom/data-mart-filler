package datamodels

case class RawNewsDTO(
                       Categories: List[String],
                       Title: String,
                       SourceId: String,
                       PublishedDateInstant: String
                  )
