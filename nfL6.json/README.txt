
nfL6: Yahoo Non-Factoid Question Dataset

The dataset is a json file containing 87,361 questions and their corresponding 
answers selected from Yahoo's Webscope L6 collection using machine learning techniques
such that the questions would contain non-factoid answers.

Each question is accompanied with its best answer and additional other answers
submitted by users.  Only the best answer was examined when determining the quality
of the question-answer pair.

Each component of a question may be accessed with specific keywords to access its parts.
For example, the data in one question dictionary inside the json file may be accessed as
follows:

  obj['question']                The question string
  obj['answer']                  The highest voted answer string
  obj['nbestanswers']            A list of strings representing other submitted answers 
                                 for that question
  obj['main_category']           A string representing the submitted question
  obj['id']                      A unique Yahoo ID string for the question


An example of a script parsing this collection in python is shown below:

  import json

  questions = []
  mydata = json.load(open('nfL6.json','r'))

  for q_a in mydata:
	questions.append(q_a['questions'])


We would like to thank Yahoo for collecting and distributing Webscope L6.
