library(caret)
library(doMC)
dat <- read.csv("/home/home3/andrew/Documents/spring17/newnew.csv", header = F)

dat$V94 <- factor(dat$V94)
dat$V1 <- NULL
dat$V2 <- NULL
dat$V3 <- NULL

set.seed(1003)
trIdx <- createDataPartition(dat$V94, p=0.8, list=FALSE, times = 1)

trSet <- dat[trIdx,]
tsSet <- dat[-trIdx,]

ctl <- trainControl(method = "repeatedcv", number = 5, repeats = 2, verboseIter = TRUE)

registerDoMC(cores = 8)
set.seed(4621)
mod <- train(V94~. , data=trSet , method = "kknn", trControl = ctl)
mod
res <- predict(mod,tsSet)

confusionMatrix(data= res, reference = tsSet$V94, positive = "1")
