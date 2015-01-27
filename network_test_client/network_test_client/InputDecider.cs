using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;

namespace network_test_client
{
    enum InputDecision
    {
        Manually,
        Predefined,
        Randomly
    }

    enum InputType
    {
        Login,
        Password
    }
    
    static class InputDecider
    {
        public static string ProcessInput(InputType inputType)
        {
            InputDecision[] inputVariants = (InputDecision[])Enum.GetValues(typeof(InputDecision));
            InputDecision resultDecision = InputDecision.Predefined;
            while (true)
            {
                CommonHelpers.PrintColoredLine(string.Format("Select input variant for {0}", inputType), ConsoleColor.Yellow);
                for (int variantIndex = 0; variantIndex < inputVariants.Length; ++variantIndex)
                    CommonHelpers.PrintColoredLine(string.Format("{0} {1}", variantIndex, inputVariants[variantIndex]), ConsoleColor.White);
                Console.WriteLine();

                var pressedKey = Console.ReadKey();
                int choiseIndex = pressedKey.KeyChar - '0';
                if ((choiseIndex >= 0) && (choiseIndex < inputVariants.Length))
                {
                    resultDecision = inputVariants[choiseIndex];                    
                    break;
                }
                CommonHelpers.PrintColoredLine("Incorrect input, try again", ConsoleColor.Red);
            }
            Console.WriteLine();

            switch (resultDecision)
            {
                case InputDecision.Manually:
                    {
                        CommonHelpers.PrintColored(string.Format("Enter message for {0}: ", inputType.ToString()), ConsoleColor.Yellow);
                        return Console.ReadLine();
                    }
                case InputDecision.Predefined:
                    {
                        if (inputType == InputType.Login)
                            return "login";
                        return "password";
                    }
                case InputDecision.Randomly:
                    {
                        Random rand = new Random((int)DateTime.Now.ToFileTime());
                        int valueLength = 3 + rand.Next(7);
                        const int alphabetLength = 'z' - 'a' + 1;

                        string value = string.Empty;
                        for (int charIndex = 0; charIndex < valueLength; ++charIndex)
                        {
                            value += (char)('a' + rand.Next(alphabetLength));
                        }
                        return value;
                    }
            }

            Debug.Assert(false); // unsupported variant of 'resultDecision' variable
            return "some_random_string";
        }
    }
}
