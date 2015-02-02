using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Diagnostics;
using network_test_client.network;

namespace network_test_client
{
    class Program
    {
        static void Main(string[] args)
        {
            SortedDictionary<UserAction, network.INetworkAction> networkActions = new SortedDictionary<UserAction, INetworkAction>();
            networkActions.Add(UserAction.LicenseRequest, new LicensingAction());
            networkActions.Add(UserAction.ExerciseScoreRequest, new ExerciseScoreAction());
            networkActions.Add(UserAction.DiaryRequest, new DiaryAction());

            while (true)
            {
                Console.WriteLine();
                CommonHelpers.PrintColoredLine("Input one of the following commands:", ConsoleColor.Yellow);           

                CommonHelpers.PrintColored(CommandLineParser.GetCommandLineUsage(), ConsoleColor.White);
                Console.WriteLine();

                UserAction action;
                try
                {
                    action = CommandLineParser.ParseCommandLine(Console.ReadLine());
                }
                catch (ArgumentException)
                {
                    Console.WriteLine("Invalid input");
                    continue;
                }
                                
                if (action == UserAction.Exit)
                    break;

                if (!networkActions.ContainsKey(action))
                {
                    CommonHelpers.PrintColoredLine("No such action is found", ConsoleColor.Red);
                    continue;
                }

                INetworkAction networkAction = networkActions[action];
                Debug.Assert(networkAction != null);

                try
                {
                    CommonHelpers.PrintColoredLine("Skip url input Y/N?", ConsoleColor.Yellow);
                    CommonHelpers.PrintColoredLine(string.Format("Current url: {0}",
                        CommonHelpers.ReturnEmptyIfNull(networkAction.GetUrl())), ConsoleColor.Green);
                    if (!CommonHelpers.ProcessYesNoChoise())
                    {
                        CommonHelpers.PrintColoredLine("Input url: ", ConsoleColor.Yellow);
                        networkAction.SetUrl(Console.ReadLine());
                    }
                    else
                    {
                        CommonHelpers.PrintColoredLine("Url was skipped", ConsoleColor.Red);
                    }
                    networkAction.SetLogin(InputDecider.ProcessInput(InputType.Login));
                    networkAction.SetPassword(InputDecider.ProcessInput(InputType.Password));
                    networkAction.Run();
                }
                catch (Exception exp)
                {
                    Console.WriteLine("Unexpected exception occured: {0}", exp.Message);
                }
            }
        }
    }
}
