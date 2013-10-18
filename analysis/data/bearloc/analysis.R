rm(list=ls())

wifi <- read.table("wifi.csv", header=TRUE, sep=",")
semloc <- read.table("semloc.csv", header=TRUE, sep=",")

#library(ggplot2)
#
## all BSSID density in 410
#epochs410 <- unique(semloc$epoch[semloc$room=="410"])
#fmin <- function(x) min(abs(x-epochs410))
#wifi410 <- wifi[sapply(wifi$epoch, fmin) < 3000,]
#wifi410 <- wifi410[table(wifi410$BSSID)[wifi410$BSSID] > 180,]
#ggplot(wifi410, aes(x=RSSI)) + geom_density(aes(group=BSSID, colour=BSSID), alpha=0.3)
#
## all room boxplot for one AP
#wifi_ap <- wifi[wifi$BSSID=='00:22:90:39:07:16',]
#fmin <- function(x) semloc$room[which.min(abs(x-semloc$epoch))]
#room_ap <- sapply(wifi_ap$epoch, fmin)
#wifiroom <- cbind(wifi_ap, room=room_ap)
#ggplot(wifiroom, aes(factor(room), RSSI)) + geom_boxplot() + theme(axis.text.x = element_text(angle = 45, hjust = 1))

bssids <- unique(wifi$BSSID)
semepochs <- unique(semloc$epoch)

fmin <- function(x, bssid) {
	bssidwifi <- wifi[wifi$BSSID==bssid,]
	if (min(abs(x-bssidwifi$epoch)) <= 3000) {
		return(bssidwifi$RSSI[which.min(abs(x-bssidwifi$epoch))])
	} else {
		return(-150)
	}
}

names <- c("room")
fingerprints <- data.frame(room=semloc$room)
i <- 0
length(bssids)
for (bssid in bssids) {
	print(i)
	i <- i+1
	column <- sapply(semepochs, fmin, bssid)
	names <- c(bssid, names)
	fingerprints <- cbind(column, fingerprints)
}
colnames(fingerprints) <- names
write.table(fingerprints, file = "fingerprints.csv")

# PCA and plot
library(ggplot2)
fingerprints <- read.table("fingerprints.csv", header=TRUE)
fingerprints <- fingerprints[table(fingerprints$room)[fingerprints$room] > 15, ]
fingerprints <- fingerprints[,c(apply(fingerprints[,colnames(fingerprints)!="room"], 2, var, na.rm=TRUE) != 0, TRUE)]
pcamodel <- prcomp(fingerprints[,colnames(fingerprints)!="room"], scale = TRUE)
pcadata <- data.frame(scale(predict(pcamodel)[, 1:2]), room=factor(fingerprints$room))
ggplot(pcadata, aes(PC1,PC2)) + geom_point(aes(colour = room))