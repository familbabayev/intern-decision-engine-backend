### Highlights

The project is reasonably well-structured by being broken into components such as endpoints, services, exceptions.

The use of specific exceptions for invalid input scenarios is a good practice.

The code utilizes separation of concerns, modularizing input validation and loan calculation functionality

The implementation of the credit scoring and decision logic in a single method (calculateApprovedLoan) could be further refined to enhance readability and maintainability.

In the "verifyInputs" function checks following this style ```!(a <= b)``` could be just replaced with ```(a > b)``` for cleaner code.

### Most important shortcoming

Credit score calculation logic is missing in the implementation which is requirement in the task description.

