

public class Tester {

	public static void main(String[] args) {

		/*
		 * examples :

question 1 :
------------

part 2 :

maxz=x1+x2
x1-x2>=1
x1+x2<=3
-x1+x2<=1
x1-x2<=1

part 3 : dual

minz=-x1+3x2+x3+x4
-x1+x2-x3+x4>=1
x1+x2+x3-x4>=1

question 2 :
------------

part 2 :

minz=x1+x2
x1-x2>=1
x1+x2<=3
-x1+x2<=1
x1-x2<=1

question 3 :
------------

part 2 : x1=x1-x2, x2=x3-x4 (x1, x2 <- R)

maxz=x1-x2+x3-x4
x1-x2-x3+x4>=1
x1-x2+x3-x4<=3
-x1+x2+x3-x4<=1
x1-x2-x3+x4<=1

question 4 :
------------

part 2 : x1=x1-x2, x2=x3-x4 (x1, x2 <- R)

minz=x1-x2+x3-x4
x1-x2-x3+x4>=1
x1-x2+x3-x4<=3
-x1+x2+x3-x4<=1
x1-x2-x3+x4<=1

part 3 : dual

maxz=x1-3x2-x3-x4
x1-x2+x3-x4<=1
x1-x2+x3-x4>=1
-x1-x2-x3+x4<=1
-x1-x2-x3+x4>=1

question 5 :
------------

part 2 :

maxz=x1+x2
x1-x2>=1
x1+x2<=3
-x1+x2>=1

part 3 : dual

minz=-x1+3x2-x3
-x1+x2+x3>=1
x1+x2-x3>=1

question 6 :
------------

maxz=5x1-x2
-x1+x2<=0
2x1+x2>=2 (-2x1-x2<=-2)

part 2 : dual

minz=-2x2
-x1-2x2>=5
-x1+x2>=2

question 7 :
------------

part 1 :

maxz=12x1+18x2+20x3+15x4
x1+2x2+5x3+4x4<=20
2x1+x2+3x3+x4<=18
2x1+2x2+4x3+2x4<=22

part 2 :

maxz=12x1+18x2+20x3+15x4
x1+3x2-6x3+4x4<=20
-2x1+x2-3x3+x4<=18
2x1-2x2+4x3-2x4<=22

question 8 :
------------

maxz=-2x1-x2+3x3-2x4
x1+3x2-x3+2x4<=7
-x1-2x2+4x3<=12
-x1-4x2+3x3+8x4<=10

dual :

minz=7x1+12x2+10x3
x1-x2-x3>=-2
3x1-2x2-4x3>=-1
-x1+4x2+3x3>=3
2x1+8x3>=-2

		 */
		SimplexSimulation.simulate();
	}

}
