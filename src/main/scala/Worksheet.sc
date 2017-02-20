import autoscale.model.MarathonApp

val apps = List(MarathonApp("xt", List()))
val appsTime:List[List[MarathonApp]] = List(apps)

val apps2 = List(MarathonApp("2", List()))

val junto = apps2 :: appsTime