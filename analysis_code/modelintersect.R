library(caret)
library(doMC)
dat <- read.csv("/home/home3/andrew/Documents/spring17/intersections.csv", header = F)

dat$V4 <- factor(dat$V4)
dat<- na.omit(dat)

set.seed(1003)
trIdx <- createDataPartition(dat$V4, p=0.8, list=FALSE, times = 1)

trSet <- dat[trIdx,]
tsSet <- dat[-trIdx,]

ctl <- trainControl(method = "repeatedcv", number = 5, repeats = 3, verboseIter = TRUE)

registerDoMC(cores = 35)
set.seed(4621)
mod <- train(V4~. , data=trSet , method = "nnet", trControl = ctl)
mod
res <- predict(mod,tsSet)

confusionMatrix(data= res, reference = tsSet$V4, positive = "1")
