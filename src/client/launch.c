/*
 * Launch the logistic regression training process with specified config file.
 */

#include <stdio.h>
#include <sys/types.h>
#include <sys/socket.h>

int main(int argc, char * argv[]) {
  
  // Read the config files and pass the information to master using TCP.
  //
  // Config file format:
  // training_data = <trainging data path>
  // eval_data = <eval data path>
  // model_output = <model output path>
  // log_path = <logging folder path>
  // reg_constant = <regularization constant>
  // batch_size = <the batch size of mini batch gradient descent>
  
  
  int sockfd, portno;
  struct sockaddr_in serv_addr;
  struct hostent *master;
  
  if (argc != 4) {
    std::cout << "Wrong number of arguments!\n";
    printf("usage: %s <filename> <master_hostname> <port>\n", argv[0]);
  }

  std::ifstream config_file("argv[1]");
  portno = atoi(argv[3]);
  sockfd = socket(AF_INET, SOCK_STREAM, 0);
  if (sockfd < 0) {
    printf("ERROR opening socket");
  }
  master = gethostbyname(argv[2]);
  if (server == NULL) {
    printf("ERROR, host does not exists\n");
    exit(0);
  }
  bzero((char *) &serv_addr, sizeof(serv_addr));
  serv_addr.sin_family = AF_INET;
  bcopy((char *)server->h_addr, 
         (char *)&serv_addr.sin_addr.s_addr,
         server->h_length);
  serv_addr.sin_port = htons(portno);
  if (connect(sockfd,(struct sockaddr *) &serv_addr,sizeof(serv_addr)) < 0) {
    printf("ERROR connecting");
  }

  char* line;
  while (// read files) {
    
  }

  close(sockfd);
  return 0;
}
