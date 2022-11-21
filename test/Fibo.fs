BEGIN Fibonacci

%% Program to display the Fibonacci sequence up to n-th term %%

  READ(nterms) ,              :: Read a nterms number from user input
  n1 := 0 ,
  n2 := 1 ,
  count := 0 ,

  IF (nterms = 1) THEN
      PRINT(n1) ,
  ELSE
    WHILE (count < nterms) DO 
      PRINT(n1) ,
      temp := n1 + n2 ,    :: add the two previous number
      n1 := n2 ,
      n2 := temp ,         :: Update the values
      count := count + 1 ,
    END,
  END,
END