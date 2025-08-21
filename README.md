1. Clone the Project
2. open the project IDE
3. clean the maven
4. update the application.properties file with your MYSQL DATABASE
5. Run the Application
   
API OF this Application

1.Post -> http://localhost:8080/api/dataset/employees/record

Body:

{

    "name": "RAmu",
    
    "age": 15,
    
    "department": "HR"
    
}

Response:

{

	"recordId": 3,
 
	"message": "Record added successfully",
 
	"dataset": "employees"
 
}

2.Get -> http://localhost:8080/api/dataset/employees/query?groupBy=department
Response:
{

	"groupedRecords": {
 
		"Engineering": [
			{
				"id": 1,
				"name": "John Doe",
				"age": 30,
				"department": "Engineering"
			},
			{
				"id": 2,
				"name": "Narendra",
				"age": 48,
				"department": "Engineering"
			}
		],
  
		"HR": [
			{
				"id": 3,
				"name": "RAmu",
				"age": 15,
				"department": "HR"
			}
		]
	}
}

3. Get ->http://localhost:8080/api/dataset/employees/query?sortBy=age&order=asc
   Response:
   {
   
	"sortedRecords": [

		{
			"id": 3,
			"name": "RAmu",
			"age": 15,
			"department": "HR"
		},

		{
			"id": 1,
			"name": "John Doe",
			"age": 30,
			"department": "Engineering"
		},

		{
			"id": 2,
			"name": "Narendra",
			"age": 48,
			"department": "Engineering"
		}

 ]
}
