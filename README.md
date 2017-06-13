# Quora Deduplication

The code for computing the vectors is located in dl4j-examples\dl4j-examples\src\main\java\org\deeplearning4j\examples\nlp available from the following link:
https://www.dropbox.com/sh/zl7ynnosup3d6au/AABTMzYlh8V4QinA4av16thPa?dl=0

I could not upload the whole project here because it contains our test files too.

Entity resolution, also commonly referred to as deduplication or dedup, is the process of identi- fying duplicate entries within a dataset. Our class project focuses on identifying duplicate questions in the popular Q&A website Quora. We leverage two existing toolsets called word2vec and doc2vec to analyze question semantics and convert each word/question into a vector. In vector form we are able to operate on a word/question and compare it with other word/question vectors to extract similarities. We present five approaches to identifying duplicates in the Quora dataset. The first four make use of commonly used machine learning algorithms such as Random Forest and Neural Networks after vectorizing the words. The final strategy naively uses a threshold and labels the given question pair accordingly. While the machine learning methods performed modestly well, the naive threshold strategy outperformed better by a significant amount.

This project was submitted as the final project for a graduate Database Integration & Cleaning seminar at Duke University.
