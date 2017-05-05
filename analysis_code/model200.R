library(caret)
library(doMC)
dat <- cbind(read.table("/home/home3/andrew/Documents/spring17/docVecs.csv", header = F, sep = ","),
             read.table("/home/home3/andrew/Documents/spring17/GTLabels.csv", header = F, col.names = "dup"))

dat$dup <- factor(dat$dup)

set.seed(1003)
trIdx <- createDataPartition(dat$dup, p=0.8, list=FALSE, times = 1)

trSet <- dat[trIdx,]
tsSet <- dat[-trIdx,]

ctl <- trainControl(method = "repeatedcv", number = 5, repeats = 2, verboseIter = TRUE)

registerDoMC(cores = 15)
set.seed(4621)
mod <- train(dup~. , data=trSet , method = "kknn", trControl = ctl)
mod
res <- predict(mod,tsSet)

confusionMatrix(data= res, reference = tsSet$dup, positive = "1")
