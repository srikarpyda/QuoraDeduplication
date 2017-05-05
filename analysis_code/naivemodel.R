library(caret)
library(doMC)
dat <- read.csv("/home/home3/andrew/Documents/spring17/naive.csv", header = T)

dat$dup <- factor(dat$dup)

set.seed(1003)
trIdx <- createDataPartition(dat$dup, p=0.8, list=FALSE, times = 1)

trSet <- dat[trIdx,]
tsSet <- dat[-trIdx,]

ctl <- trainControl(method = "repeatedcv", number = 10, repeats = 5, verboseIter = TRUE)

registerDoMC(cores = 3)
set.seed(4621)
mod <- train(dup~ similarity , data=trSet , method = "nnet", trControl = ctl)
mod
res <- predict(mod,tsSet)

confusionMatrix(data= res, reference = tsSet$dup, positive = "1")
